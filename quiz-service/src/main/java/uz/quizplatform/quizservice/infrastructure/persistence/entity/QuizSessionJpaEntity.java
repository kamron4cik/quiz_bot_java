package uz.quizplatform.quizservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.quizplatform.quizservice.domain.valueobject.QuizMode;
import uz.quizplatform.quizservice.domain.valueobject.QuizStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for quiz_sessions table.
 *
 * questionIds is stored as a JSONB array in PostgreSQL.
 * Hibernate 6.x handles this natively with @JdbcTypeCode(SqlTypes.JSON).
 */
@Entity
@Table(name = "quiz_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "university_id")
    private UUID universityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizMode mode;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "time_per_question_seconds", nullable = false)
    private int timePerQuestionSeconds;

    @Column(name = "question_offset", nullable = false)
    private int questionOffset;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_ids", nullable = false, columnDefinition = "jsonb")
    private List<UUID> questionIds;

    @Column(name = "current_question_index", nullable = false)
    private int currentQuestionIndex;

    @Column(name = "current_poll_id")
    private String currentPollId;

    @Column(name = "current_correct_option_idx")
    private Integer currentCorrectOptionIndex;

    @Column(name = "current_question_sent_at")
    private Instant currentQuestionSentAt;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status;

    @Column(name = "total_correct", nullable = false)
    private int totalCorrect;

    @Column(name = "total_wrong", nullable = false)
    private int totalWrong;

    @Column(nullable = false)
    private BigDecimal score;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;
}
