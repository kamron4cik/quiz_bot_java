package uz.quizplatform.quizservice.infrastructure.cache;

import uz.quizplatform.quizservice.domain.entity.QuizSession;

public interface QuizSessionCache {
    void delete(Long userId);
    void put(Long userId, QuizSession session);
}
