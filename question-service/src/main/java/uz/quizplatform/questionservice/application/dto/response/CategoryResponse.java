package uz.quizplatform.questionservice.application.dto.response;

import uz.quizplatform.questionservice.domain.entity.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID universityId,
        String name,
        String description,
        boolean active,
        Instant createdAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getUniversityId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
