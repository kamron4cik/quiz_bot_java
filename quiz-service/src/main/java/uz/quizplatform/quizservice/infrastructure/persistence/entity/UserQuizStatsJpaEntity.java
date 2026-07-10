package uz.quizplatform.quizservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_quiz_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizStatsJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "tests_completed", nullable = false)
    private int testsCompleted;

    @Column(name = "questions_solved", nullable = false)
    private int questionsSolved;

    @Column(name = "total_correct", nullable = false)
    private int totalCorrect;

    @Column(name = "average_score", nullable = false)
    private BigDecimal averageScore;

    @Column(name = "best_score", nullable = false)
    private BigDecimal bestScore;

    @Column(name = "total_study_time_sec", nullable = false)
    private long totalStudyTimeSec;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
