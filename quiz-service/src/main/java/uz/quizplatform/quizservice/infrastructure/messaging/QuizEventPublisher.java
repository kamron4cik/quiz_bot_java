package uz.quizplatform.quizservice.infrastructure.messaging;

import uz.quizplatform.quizservice.domain.entity.QuizSession;

public interface QuizEventPublisher {
    void publishSessionFinished(QuizSession session);
}
