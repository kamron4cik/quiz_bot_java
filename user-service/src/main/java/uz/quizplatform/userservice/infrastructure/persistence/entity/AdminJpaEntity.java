package uz.quizplatform.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.quizplatform.userservice.domain.valueobject.AdminRole;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminJpaEntity {

    @Id
    @Column(name = "telegram_id")
    private Long telegramId;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (role == null) role = AdminRole.ADMIN;
    }
}
