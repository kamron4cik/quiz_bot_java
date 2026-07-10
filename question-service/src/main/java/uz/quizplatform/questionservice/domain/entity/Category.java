package uz.quizplatform.questionservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Category domain entity.
 *
 * A category groups questions for a specific university.
 * Universities are managed in user-service; this service stores
 * only the universityId as a cross-service reference (no FK).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private UUID id;
    private UUID universityId;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static Category create(UUID universityId, String name, String description) {
        return Category.builder()
                .universityId(universityId)
                .name(name)
                .description(description)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }
}
