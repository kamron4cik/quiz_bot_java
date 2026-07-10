package uz.quizplatform.common.domain.event;

import lombok.Getter;

/**
 * Event published by any service to request notification-service to deliver
 * a text message to a specific user via Telegram.
 */
@Getter
public class SendTextMessageEvent extends DomainEvent {

    public static final String EVENT_TYPE = "notification.send.text";

    private final Long userId;
    private final String message;
    private final String parseMode;

    public SendTextMessageEvent(
            String correlationId,
            Long userId,
            String message,
            String parseMode) {
        super(EVENT_TYPE, correlationId, "quiz-service");
        this.userId = userId;
        this.message = message;
        this.parseMode = parseMode;
    }
}
