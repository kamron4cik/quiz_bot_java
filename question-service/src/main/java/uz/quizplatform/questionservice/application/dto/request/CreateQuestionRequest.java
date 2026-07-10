package uz.quizplatform.questionservice.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uz.quizplatform.questionservice.domain.valueobject.Difficulty;

import java.util.UUID;

public record CreateQuestionRequest(
        @NotNull UUID categoryId,
        @NotBlank String text,
        @NotBlank String optionA,
        @NotBlank String optionB,
        @NotBlank String optionC,
        @NotBlank String optionD,
        @Min(0) @Max(3) int correctAnswer,
        String explanation,
        String imageUrl,
        Difficulty difficulty
) {}
