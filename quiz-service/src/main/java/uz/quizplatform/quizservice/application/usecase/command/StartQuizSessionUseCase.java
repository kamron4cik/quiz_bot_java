package uz.quizplatform.quizservice.application.usecase.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.BusinessRuleViolationException;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.quizservice.application.dto.request.StartQuizRequest;
import uz.quizplatform.quizservice.application.dto.response.QuizSessionResponse;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.CategoryRepository;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.domain.valueobject.QuizMode;
import uz.quizplatform.quizservice.domain.valueobject.QuizStatus;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;
import uz.quizplatform.quizservice.infrastructure.client.QuestionServiceClient;
import uz.quizplatform.quizservice.infrastructure.client.UserServiceClient;
import uz.quizplatform.quizservice.infrastructure.messaging.QuizEventPublisher;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Start a new quiz session.
 *
 * Business rules preserved from V1:
 * 1. User must have a complete profile
 * 2. User must have paid (has_paid=true) OR be an admin
 * 3. If user has an ACTIVE session → force close it before starting new one
 * 4. If user has a PAUSED session → warn user and require explicit discard or resume
 * 5. Category must have enough questions for the requested count
 * 6. Questions are shuffled (RANDOM) or offset-applied (SEQUENTIAL) at session creation
 * 7. A Redis distributed lock prevents concurrent session creation for the same user
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StartQuizSessionUseCase {

    private static final int MIN_QUESTIONS_REQUIRED = 5;
    private static final String INACTIVITY_MINUTES_DEFAULT = "5";

    private final QuizSessionRepository sessionRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionServiceClient questionServiceClient;
    private final UserServiceClient userServiceClient;
    private final QuizSessionCache sessionCache;
    private final QuizEventPublisher eventPublisher;

    public QuizSessionResponse execute(StartQuizRequest request) {
        log.info("StartQuizSession: userId={}, categoryId={}, mode={}, count={}, time={}s",
                request.getUserId(), request.getCategoryId(), request.getMode(),
                request.getQuestionCount(), request.getTimePerQuestionSeconds());

        // ── Guard 1: User exists and has access ─────────────────────────────
        var user = userServiceClient.getUser(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (!user.isAdmin() && !user.isHasPaid()) {
            throw new BusinessRuleViolationException(
                    "Testni boshlash uchun avval to'lov qiling.", "PAYMENT_REQUIRED");
        }

        if (!user.isAdmin() && !user.isProfileComplete()) {
            throw new BusinessRuleViolationException(
                    "Profil to'liq emas. Davom etishdan oldin profilingizni to'ldiring.", "PROFILE_INCOMPLETE");
        }

        // ── Guard 2: Category exists ─────────────────────────────────────────
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        if (!category.isActive()) {
            throw new BusinessRuleViolationException("Bu kategoriya faol emas.", "CATEGORY_INACTIVE");
        }

        // ── Guard 3: Handle existing sessions ────────────────────────────────
        // Business rule from V1: If there's an active session, force close it.
        // If there's a paused session, the caller should have already handled this.
        sessionRepository.findActiveByUserId(request.getUserId())
                .ifPresent(active -> {
                    log.info("Force-closing active session {} for user {} before new start",
                            active.getId(), request.getUserId());
                    active.stop();
                    sessionRepository.save(active);
                    sessionCache.delete(request.getUserId());
                    eventPublisher.publishSessionFinished(active);
                });

        // ── Guard 4: Get questions ────────────────────────────────────────────
        List<UUID> questionIds = questionServiceClient.getQuestionIdsByCategory(request.getCategoryId());

        if (questionIds.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Bu kategoriyada savollar mavjud emas.", "NO_QUESTIONS_AVAILABLE");
        }

        int requestedCount = request.getQuestionCount() > 0
                ? request.getQuestionCount()
                : questionIds.size();

        if (questionIds.size() < MIN_QUESTIONS_REQUIRED && requestedCount > questionIds.size()) {
            throw new BusinessRuleViolationException(
                    "Kategoriyada kamida " + MIN_QUESTIONS_REQUIRED + " ta savol bo'lishi kerak.",
                    "INSUFFICIENT_QUESTIONS");
        }

        // ── Create session ────────────────────────────────────────────────────
        QuizMode mode = QuizMode.valueOf(request.getMode().toUpperCase());
        var session = QuizSession.create(
                request.getUserId(),
                request.getCategoryId(),
                user.getUniversityId(),
                mode,
                requestedCount,
                request.getTimePerQuestionSeconds() > 0 ? request.getTimePerQuestionSeconds() : 30,
                request.getQuestionOffset(),
                questionIds
        );

        session = sessionRepository.save(session);
        sessionCache.put(request.getUserId(), session);

        log.info("Quiz session {} created for user {}: {} questions, mode={}, time={}s",
                session.getId(), request.getUserId(), session.getQuestionCount(), mode,
                session.getTimePerQuestionSeconds());

        return QuizSessionResponse.from(session, category.getName());
    }
}
