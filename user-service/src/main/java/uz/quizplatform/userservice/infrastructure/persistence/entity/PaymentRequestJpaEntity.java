package uz.quizplatform.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.quizplatform.userservice.domain.valueobject.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "major")
    private String major;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "study_method")
    private String studyMethod;

    @Column(name = "test_type")
    private String testType;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "receipt_file_id", nullable = false)
    private String receiptFileId;

    @Column(name = "receipt_storage_path")
    private String receiptStoragePath;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = PaymentStatus.PENDING;
    }
}
