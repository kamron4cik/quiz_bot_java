package uz.quizplatform.questionservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotNull UUID universityId,
        @NotBlank String name,
        String description
) {}
