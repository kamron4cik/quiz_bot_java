package uz.quizplatform.telegramservice.infrastructure.state;

public interface UserStateManager {
    void setPendingNewQuiz(Long userId, boolean pending);
}
