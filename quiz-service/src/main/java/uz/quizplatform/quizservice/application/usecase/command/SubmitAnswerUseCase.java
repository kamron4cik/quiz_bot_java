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
import uz.quizplatform.quizservice.infrastructure.messaging.QuizEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubmitAnswerUseCase {

    private final QuizSessionRepository sessionRepository;
    private final QuizSessionCache sessionCache;
    private final QuizEventPublisher eventPublisher;
    private final NotificationServiceClient notificationClient;
    private final uz.quizplatform.quizservice.application.service.UserQuizStatsService statsService;
    private final uz.quizplatform.quizservice.application.service.LeaderboardService leaderboardService;

    public SubmitAnswerResponse execute(String pollId, Long userId, int optionId) {
        log.info("SubmitAnswer: pollId={}, userId={}, optionId={}", pollId, userId, optionId);

        QuizSession session = sessionRepository.findActiveByPollId(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession for poll", pollId));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Submitted user ID does not match session user ID");
        }

        boolean isCorrect = session.getCurrentCorrectOptionIndex() != null && optionId == session.getCurrentCorrectOptionIndex();
        String selectedAnswer = session.getCurrentShuffledOptions() != null && optionId < session.getCurrentShuffledOptions().size()
                ? session.getCurrentShuffledOptions().get(optionId)
                : "Option " + optionId;

        session.recordAnswer(selectedAnswer, isCorrect);

        if (!session.hasMoreQuestions()) {
            session.complete();
            sessionRepository.save(session);
            sessionCache.delete(userId);
            
            log.info("Session {} completed. Score: {}%", session.getId(), session.getScore());
            statsService.updateStats(session);
            leaderboardService.updateLeaderboard(session);
            
            eventPublisher.publishSessionFinished(session);
            notificationClient.sendSessionResults(session);
            
            return new SubmitAnswerResponse(true, session.getId().toString());
        } else {
            sessionRepository.save(session);
            sessionCache.put(userId, session);
            
            log.info("Session {} advanced to question {}", session.getId(), session.getCurrentQuestionIndex());
            notificationClient.sendNextQuestion(session);
            
            return new SubmitAnswerResponse(false, session.getId().toString());
        }
    }

    public record SubmitAnswerResponse(boolean isSessionFinished, String sessionId) {}
}
