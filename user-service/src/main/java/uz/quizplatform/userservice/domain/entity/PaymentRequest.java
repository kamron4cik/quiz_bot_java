package uz.quizplatform.userservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.quizplatform.userservice.domain.valueobject.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * PaymentRequest domain entity.
 * Represents a user's request to access the platform by submitting a payment receipt.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private UUID id;
    private Long userId;
    private UUID universityId;

    // Snapshot of user profile at time of payment
    private String major;
    private Integer grade;
    private String studyMethod;
    private String testType;

    // Payment details
    private int amount;
    private String receiptFileId;
    private String receiptStoragePath;
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private Instant createdAt;
    private Instant reviewedAt;
    private Long reviewedBy;

    public void approve(Long adminId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING payment requests");
        }
        this.status = PaymentStatus.APPROVED;
        this.reviewedAt = Instant.now();
        this.reviewedBy = adminId;
    }

    public void reject(Long adminId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING payment requests");
        }
        this.status = PaymentStatus.REJECTED;
        this.reviewedAt = Instant.now();
        this.reviewedBy = adminId;
    }
}
