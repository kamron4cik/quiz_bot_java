package uz.quizplatform.common.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Raised when a user completes, stops, or times out a quiz session.
 * Consumed by: notification-service (send results), quiz-service (update leaderboard), user-service (update stats).
 */
@Getter
public class QuizSessionFinishedEvent extends DomainEvent {

    public static final String EVENT_TYPE = "quiz.session.finished";

    private final String sessionId;
    private final Long userId;
    private final UUID categoryId;
    private final String categoryName;
    private final int questionCount;
    private final int totalCorrect;
    private final int totalWrong;
    private final int scorePercentage;
    private final long durationSeconds;
    private final String finishReason; // COMPLETED, STOPPED, TIMEOUT

    public QuizSessionFinishedEvent(
            String correlationId,
            String sessionId,
            Long userId,
            UUID categoryId,
            String categoryName,
            int questionCount,
            int totalCorrect,
            int totalWrong,
            int scorePercentage,
            long durationSeconds,
            String finishReason) {
        super(EVENT_TYPE, correlationId, "quiz-service");
        this.sessionId = sessionId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.questionCount = questionCount;
        this.totalCorrect = totalCorrect;
        this.totalWrong = totalWrong;
        this.scorePercentage = scorePercentage;
        this.durationSeconds = durationSeconds;
        this.finishReason = finishReason;
    }
}
