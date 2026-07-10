package uz.quizplatform.questionservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.quizplatform.questionservice.domain.valueobject.Difficulty;

import java.time.Instant;
import java.util.UUID;

/**
 * Question domain entity.
 *
 * Represents a single multiple-choice question with 4 options (A/B/C/D).
 * The correctAnswer field is 0-indexed: 0=A, 1=B, 2=C, 3=D.
 * This maps directly to the Telegram Poll API's selectedOption index.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    private UUID id;
    private UUID categoryId;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    /** 0=A, 1=B, 2=C, 3=D — matches Telegram poll selectedOption index */
    private int correctAnswer;
    private String explanation;
    private String imageUrl;
    private Difficulty difficulty;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static Question create(UUID categoryId, String text,
                                   String optionA, String optionB,
                                   String optionC, String optionD,
                                   int correctAnswer, String explanation,
                                   String imageUrl, Difficulty difficulty) {
        if (correctAnswer < 0 || correctAnswer > 3) {
            throw new IllegalArgumentException("correctAnswer must be between 0 and 3");
        }
        return Question.builder()
                .categoryId(categoryId)
                .text(text)
                .optionA(optionA)
                .optionB(optionB)
                .optionC(optionC)
                .optionD(optionD)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .imageUrl(imageUrl)
                .difficulty(difficulty != null ? difficulty : Difficulty.MEDIUM)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void softDelete() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /** Returns all 4 options as an ordered array — index matches correctAnswer */
    public String[] getOptions() {
        return new String[]{optionA, optionB, optionC, optionD};
    }
}
