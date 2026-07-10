package uz.quizplatform.telegramservice.infrastructure.client;

import java.util.List;
import java.util.UUID;

public interface QuizServiceClient {
    Session getPausedSession(Long userId);
    List<CategoryDto> getCategoriesForUser(Long userId);
    Stats getUserStats(Long userId);
    String getUserRank(Long userId, UserServiceClient.User user);
    void resumeSession(Long userId);
    void pauseSession(Long userId);
    void startSession(Long userId, UUID categoryId);
    SubmitPollAnswerResponse submitPollAnswer(String pollId, Long userId, int optionId);

    record Session(String categoryName, int currentQuestionIndex, int questionCount) {}
    record Stats(int testsCompleted, int questionsSolved, int averageScore, int bestScore, long totalStudyTimeSec) {}
    record SubmitPollAnswerResponse(boolean isSessionFinished, String sessionId) {}
    record CategoryDto(UUID id, String name) {}
}
