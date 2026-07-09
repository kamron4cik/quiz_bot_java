package uz.quizplatform.common.domain.event;

import lombok.Getter;

/**
 * Raised when a payment request is approved or rejected.
 * Consumed by: user-service (grant access), notification-service (notify user).
 */
@Getter
public class PaymentStatusChangedEvent extends DomainEvent {

    public static final String EVENT_TYPE = "payment.status.changed";

    private final String paymentId;
    private final Long userId;
    private final String newStatus; // APPROVED, REJECTED
    private final String adminId;

    public PaymentStatusChangedEvent(
            String correlationId,
            String paymentId,
            Long userId,
            String newStatus,
            String adminId) {
        super(EVENT_TYPE, correlationId, "admin-service");
        this.paymentId = paymentId;
        this.userId = userId;
        this.newStatus = newStatus;
        this.adminId = adminId;
    }
}
