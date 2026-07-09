package uz.quizplatform.quizservice.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;
import uz.quizplatform.quizservice.infrastructure.messaging.QuizEventPublisher;
import uz.quizplatform.quizservice.infrastructure.client.NotificationServiceClient;

import java.util.List;

/**
 * Background scheduler for quiz session timeout management.
 *
 * Replaces V1's setInterval(() => quizService.runBackgroundChecks(), 15000).
 *
 * Runs every 15 seconds and checks ALL active quiz sessions for:
 * 1. Question timeout  → marks current question wrong, auto-advances
 * 2. Inactivity timeout → closes the entire session after 5 minutes of no activity
 *
 * Thread-safe: Spring's @Scheduled runs in a single thread by default.
 * For multi-instance deployments, the UNIQUE index on quiz_sessions(user_id) WHERE status='ACTIVE'
 * ensures only one session per user exists at the DB level.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizTimeoutScheduler {

    private static final int INACTIVITY_TIMEOUT_MINUTES = 5;

    private final QuizSessionRepository sessionRepository;
    private final QuizSessionCache sessionCache;
    private final QuizEventPublisher eventPublisher;
    private final NotificationServiceClient notificationClient;

    /**
     * Main background check — runs every 15 seconds.
     * Mirrors V1's quizService.runBackgroundChecks() exactly.
     */
    @Scheduled(fixedDelay = 15_000)
    @Transactional
    public void runTimeoutChecks() {
        List<QuizSession> activeSessions = sessionRepository.findAllActive();
        if (activeSessions.isEmpty()) return;

        log.debug("Timeout check: {} active sessions", activeSessions.size());

        for (QuizSession session : activeSessions) {
            try {
                processSession(session);
            } catch (Exception e) {
                log.error("Error processing session {} during timeout check", session.getId(), e);
            }
        }
    }

    private void processSession(QuizSession session) {
        // Check inactivity timeout (5 minutes with no question sent)
        if (session.isInactive(INACTIVITY_TIMEOUT_MINUTES)) {
            log.info("Inactivity timeout for session {} (user {})", session.getId(), session.getUserId());
            handleInactivityTimeout(session);
            return;
        }

        // Check per-question timeout
        if (session.isCurrentQuestionTimedOut()) {
            log.debug("Question timeout for session {} question index {}",
                    session.getId(), session.getCurrentQuestionIndex());
            handleQuestionTimeout(session);
        }
    }

    private void handleQuestionTimeout(QuizSession session) {
        session.recordTimeout();

        if (!session.hasMoreQuestions()) {
            // All questions exhausted → complete the session
            session.complete();
            sessionRepository.save(session);
            sessionCache.delete(session.getUserId());
            eventPublisher.publishSessionFinished(session);
            notificationClient.sendSessionResults(session);
            log.info("Session {} completed via question timeout", session.getId());
        } else {
            // Advance to next question
            sessionRepository.save(session);
            sessionCache.put(session.getUserId(), session);

            // Request notification-service to send the next question
            notificationClient.sendNextQuestion(session);
            log.debug("Advanced to question {} in session {} after timeout",
                    session.getCurrentQuestionIndex(), session.getId());
        }
    }

    private void handleInactivityTimeout(QuizSession session) {
        session.timeout();
        sessionRepository.save(session);
        sessionCache.delete(session.getUserId());
        eventPublisher.publishSessionFinished(session);
        notificationClient.sendInactivityTimeoutNotification(session);
        log.info("Session {} closed due to inactivity (user {})", session.getId(), session.getUserId());
    }
}
