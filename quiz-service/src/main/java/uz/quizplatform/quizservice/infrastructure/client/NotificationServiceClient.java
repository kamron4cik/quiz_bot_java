package uz.quizplatform.quizservice.infrastructure.client;

import uz.quizplatform.quizservice.domain.entity.QuizSession;

public interface NotificationServiceClient {
    void sendTimeoutWarning(Long userId, String message);
    void sendSessionResults(QuizSession session);
    void sendNextQuestion(QuizSession session);
    void sendInactivityTimeoutNotification(QuizSession session);
}
