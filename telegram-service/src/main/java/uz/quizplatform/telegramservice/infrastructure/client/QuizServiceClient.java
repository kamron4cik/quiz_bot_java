package uz.quizplatform.telegramservice.infrastructure.client;

import java.util.List;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;

public interface QuizServiceClient {
    Session getPausedSession(Long userId);
    List<Object> getCategoriesForUser(Long userId);
    Stats getUserStats(Long userId);
    String getUserRank(Long userId, Object user);
    void resumeSession(Long userId);
    SubmitPollAnswerResponse submitPollAnswer(String pollId, Long userId, int optionId);

    record Session(String categoryName, int currentQuestionIndex, int questionCount) {}
    record Stats(int testsCompleted, int questionsSolved, int averageScore, int bestScore, long totalStudyTimeSec) {}
    record SubmitPollAnswerResponse(boolean isSessionFinished, String sessionId) {}
}
