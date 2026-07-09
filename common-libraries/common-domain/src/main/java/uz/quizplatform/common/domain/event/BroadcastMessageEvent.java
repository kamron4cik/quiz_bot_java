package uz.quizplatform.common.domain.event;

import lombok.Getter;

import java.util.List;

/**
 * Raised when an admin initiates a broadcast to all users.
 * Consumed by: notification-service (batched delivery via RabbitMQ).
 */
@Getter
public class BroadcastMessageEvent extends DomainEvent {

    public static final String EVENT_TYPE = "notification.broadcast";

    private final String message;
    private final String parseMode; // HTML or MarkdownV2
    private final List<Long> targetUserIds; // empty = all users
    private final Long initiatedByAdminId;

    public BroadcastMessageEvent(
            String correlationId,
            String message,
            String parseMode,
            List<Long> targetUserIds,
            Long initiatedByAdminId) {
        super(EVENT_TYPE, correlationId, "admin-service");
        this.message = message;
        this.parseMode = parseMode;
        this.targetUserIds = targetUserIds;
        this.initiatedByAdminId = initiatedByAdminId;
    }
}
