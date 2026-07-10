package uz.quizplatform.common.domain.event;

import lombok.Getter;
import java.util.List;
import java.util.UUID;

/**
 * Event published by quiz-service to request notification-service to deliver
 * a native Telegram poll/quiz question to the user.
 */
@Getter
public class SendQuizQuestionEvent extends DomainEvent {

    public static final String EVENT_TYPE = "quiz.send.question";

    private final Long userId;
    private final UUID sessionId;
    private final UUID questionId;
    private final String questionText;
    private final List<String> options;
    private final int correctOptionIndex;
    private final int timeLimitSeconds;

    public SendQuizQuestionEvent(
            String correlationId,
            Long userId,
            UUID sessionId,
            UUID questionId,
            String questionText,
            List<String> options,
            int correctOptionIndex,
            int timeLimitSeconds) {
        super(EVENT_TYPE, correlationId, "quiz-service");
        this.userId = userId;
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.timeLimitSeconds = timeLimitSeconds;
    }
}
