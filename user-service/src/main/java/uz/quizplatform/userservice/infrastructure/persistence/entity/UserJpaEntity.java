package uz.quizplatform.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the users table.
 * Intentionally separate from the domain User entity to maintain Clean Architecture.
 * The infrastructure mapper converts between these two representations.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {

    @Id
    @Column(name = "id")
    private Long id; // Telegram ID

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "major")
    private String major;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "study_method")
    @Enumerated(EnumType.STRING)
    private uz.quizplatform.userservice.domain.valueobject.StudyMethod studyMethod;

    @Column(name = "test_type")
    @Enumerated(EnumType.STRING)
    private uz.quizplatform.userservice.domain.valueobject.TestType testType;

    @Column(name = "has_paid", nullable = false)
    @Builder.Default
    private boolean hasPaid = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_activity")
    private Instant lastActivity;

    @Column(name = "total_tests")
    @Builder.Default
    private int totalTests = 0;

    @Column(name = "total_questions")
    @Builder.Default
    private int totalQuestions = 0;

    @Column(name = "average_score")
    @Builder.Default
    private double averageScore = 0.0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (lastActivity == null) lastActivity = Instant.now();
    }
}
