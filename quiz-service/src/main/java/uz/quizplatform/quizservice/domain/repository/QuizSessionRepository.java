package uz.quizplatform.quizservice.domain.repository;

import uz.quizplatform.quizservice.domain.entity.QuizSession;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository {
    Optional<QuizSession> findActiveByUserId(Long userId);
    Optional<QuizSession> findPausedByUserId(Long userId);
    Optional<QuizSession> findActiveByPollId(String pollId);
    List<QuizSession> findAllActive();
    QuizSession save(QuizSession session);
}
