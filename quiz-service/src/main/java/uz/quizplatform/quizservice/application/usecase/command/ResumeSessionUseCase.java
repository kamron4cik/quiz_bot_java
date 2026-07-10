package uz.quizplatform.quizservice.application.usecase.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;
import uz.quizplatform.quizservice.infrastructure.client.NotificationServiceClient;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResumeSessionUseCase {

    private final QuizSessionRepository sessionRepository;
    private final QuizSessionCache sessionCache;
    private final NotificationServiceClient notificationClient;

    public void execute(Long userId) {
        log.info("Resuming paused quiz session for user {}", userId);

        QuizSession session = sessionRepository.findPausedByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Paused QuizSession for user", userId));

        session.resume();
        sessionRepository.save(session);
        sessionCache.put(userId, session);

        log.info("Session {} resumed. Re-sending current question index {}", session.getId(), session.getCurrentQuestionIndex());
        notificationClient.sendNextQuestion(session);
    }
}
