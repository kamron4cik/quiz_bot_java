package uz.quizplatform.userservice.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * University domain entity.
 * Represents one of the multi-tenant institutions (TMI, TDIU, TEAM, WIUT, etc.)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class University {

    private UUID id;
    private String name;
    private Instant createdAt;

    public static University create(String name) {
        return University.builder()
                .id(UUID.randomUUID())
                .name(name)
                .createdAt(Instant.now())
                .build();
    }
}
