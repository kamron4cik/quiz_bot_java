package uz.quizplatform.quizservice.domain.entity;

import lombok.*;
import uz.quizplatform.quizservice.domain.valueobject.QuizMode;
import uz.quizplatform.quizservice.domain.valueobject.QuizStatus;

import java.time.Instant;
import java.util.*;

/**
 * QuizSession — the core aggregate root of the quiz domain.
 *
 * Encapsulates all business rules from V1:
 * - Single active session per user (enforced by DB unique index + domain invariant)
 * - Answer shuffling (Fisher-Yates done at session creation)
 * - Question timeout (per-question timer)
 * - Inactivity timeout (5-min global inactivity)
 * - Pause/Resume with state preservation
 * - Sequential mode (user-defined offset) vs Random mode
 * - Score calculation (percentage of correct out of total answered)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession {

    private UUID id;
    private Long userId;
    private UUID categoryId;
    private UUID universityId;

    // Configuration
    private QuizMode mode;
    private int questionCount;
    private int timePerQuestionSeconds;
    private int questionOffset; // for sequential mode

    // Question sequence
    private List<UUID> questionIds;         // ordered list of question UUIDs
    private int currentQuestionIndex;       // 0-based

    // Current question Telegram state
    private String currentPollId;           // Telegram native poll ID
    private List<String> currentShuffledOptions; // labels in shuffled order (A,B,C,D)
    private Integer currentCorrectOptionIndex;
    private Instant currentQuestionSentAt;
    private Long lastMessageId;             // For editing/deleting the stop button message

    // Status
    private QuizStatus status;

    // Results
    private int totalCorrect;
    private int totalWrong;
    private double score;  // percentage

    // Timestamps
    private Instant startedAt;
    private Instant finishedAt;

    // ─── Factory ─────────────────────────────────────────────

    /**
     * Creates a new quiz session.
     * Questions are pre-shuffled here if mode is RANDOM.
     * For SEQUENTIAL mode, questions start from the given offset.
     */
    public static QuizSession create(
            Long userId,
            UUID categoryId,
            UUID universityId,
            QuizMode mode,
            int questionCount,
            int timePerQuestionSeconds,
            int questionOffset,
            List<UUID> allQuestionIds) {

        List<UUID> selectedIds = selectQuestions(allQuestionIds, mode, questionCount, questionOffset);

        return QuizSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .categoryId(categoryId)
                .universityId(universityId)
                .mode(mode)
                .questionCount(selectedIds.size())
                .timePerQuestionSeconds(timePerQuestionSeconds)
                .questionOffset(questionOffset)
                .questionIds(Collections.unmodifiableList(selectedIds))
                .currentQuestionIndex(0)
                .status(QuizStatus.ACTIVE)
                .totalCorrect(0)
                .totalWrong(0)
                .score(0.0)
                .startedAt(Instant.now())
                .build();
    }

    private static List<UUID> selectQuestions(
            List<UUID> allIds,
            QuizMode mode,
            int count,
            int offset) {
        List<UUID> mutable = new ArrayList<>(allIds);
        if (mode == QuizMode.RANDOM) {
            Collections.shuffle(mutable, new Random());
        }
        // Apply offset for sequential mode; wrap around if necessary
        if (offset > 0 && offset < mutable.size()) {
            List<UUID> rotated = new ArrayList<>();
            rotated.addAll(mutable.subList(offset, mutable.size()));
            rotated.addAll(mutable.subList(0, offset));
            mutable = rotated;
        }
        int limit = Math.min(count, mutable.size());
        return mutable.subList(0, limit);
    }

    // ─── Business Methods ─────────────────────────────────────

    public UUID getCurrentQuestionId() {
        if (currentQuestionIndex >= questionIds.size()) return null;
        return questionIds.get(currentQuestionIndex);
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questionIds.size();
    }

    public boolean isLastQuestion() {
        return currentQuestionIndex == questionIds.size() - 1;
    }

    public int getRemainingQuestions() {
        return Math.max(0, questionIds.size() - currentQuestionIndex);
    }

    /**
     * Records an answer and advances to the next question.
     * Returns true if the answer was correct.
     */
    public boolean recordAnswer(String selectedAnswer, boolean isCorrect) {
        if (isCorrect) {
            this.totalCorrect++;
        } else {
            this.totalWrong++;
        }
        this.currentQuestionIndex++;
        this.currentPollId = null;
        this.currentShuffledOptions = null;
        this.currentCorrectOptionIndex = null;
        this.currentQuestionSentAt = null;
        return isCorrect;
    }

    /**
     * Records a timeout (unanswered question) and advances.
     */
    public void recordTimeout() {
        this.totalWrong++;
        this.currentQuestionIndex++;
        this.currentPollId = null;
        this.currentShuffledOptions = null;
        this.currentCorrectOptionIndex = null;
        this.currentQuestionSentAt = null;
    }

    /**
     * Marks the poll as sent for the current question.
     */
    public void markQuestionSent(String pollId, List<String> shuffledOptions, int correctOptionIndex, Long messageId) {
        this.currentPollId = pollId;
        this.currentShuffledOptions = shuffledOptions;
        this.currentCorrectOptionIndex = correctOptionIndex;
        this.currentQuestionSentAt = Instant.now();
        this.lastMessageId = messageId;
    }

    public void complete() {
        this.status = QuizStatus.COMPLETED;
        this.finishedAt = Instant.now();
        calculateScore();
    }

    public void stop() {
        this.status = QuizStatus.STOPPED;
        this.finishedAt = Instant.now();
        calculateScore();
    }

    public void timeout() {
        this.status = QuizStatus.TIMEOUT;
        this.finishedAt = Instant.now();
        calculateScore();
    }

    public void pause() {
        if (this.status != QuizStatus.ACTIVE) {
            throw new IllegalStateException("Can only pause an ACTIVE session, current: " + this.status);
        }
        this.status = QuizStatus.PAUSED;
    }

    public void resume() {
        if (this.status != QuizStatus.PAUSED) {
            throw new IllegalStateException("Can only resume a PAUSED session, current: " + this.status);
        }
        this.status = QuizStatus.ACTIVE;
    }

    /**
     * Business rule: is the current question timed out?
     * Called by the background scheduler every 15 seconds.
     */
    public boolean isCurrentQuestionTimedOut() {
        if (currentQuestionSentAt == null || status != QuizStatus.ACTIVE) return false;
        return Instant.now().isAfter(currentQuestionSentAt.plusSeconds(timePerQuestionSeconds));
    }

    /**
     * Business rule: has the session been inactive for more than 5 minutes?
     * This covers the case where the user abandons the quiz entirely.
     */
    public boolean isInactive(int inactivityMinutes) {
        if (status != QuizStatus.ACTIVE) return false;
        Instant lastActivity = currentQuestionSentAt != null ? currentQuestionSentAt : startedAt;
        return Instant.now().isAfter(lastActivity.plusSeconds((long) inactivityMinutes * 60));
    }

    private void calculateScore() {
        int total = totalCorrect + totalWrong;
        this.score = total > 0 ? Math.round((double) totalCorrect / total * 100.0 * 10) / 10.0 : 0.0;
    }

    public long getDurationSeconds() {
        Instant end = finishedAt != null ? finishedAt : Instant.now();
        return end.getEpochSecond() - startedAt.getEpochSecond();
    }

    public boolean isTerminal() {
        return status == QuizStatus.COMPLETED
                || status == QuizStatus.STOPPED
                || status == QuizStatus.TIMEOUT;
    }
}
