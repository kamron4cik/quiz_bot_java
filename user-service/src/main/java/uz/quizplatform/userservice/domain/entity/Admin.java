package uz.quizplatform.userservice.domain.entity;

import lombok.*;
import uz.quizplatform.userservice.domain.valueobject.AdminRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Admin domain entity.
 *
 * An admin is a Telegram user who has been granted administrative privileges
 * by a SUPER_ADMIN via direct database insertion.
 *
 * Business rules:
 * - Every admin is scoped to exactly one university (except SUPER_ADMIN who has universityId=null)
 * - Admins bypass user profile checks and payment requirements
 * - Admins can only manage content belonging to their university
 * - SUPER_ADMIN can manage all universities
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    private Long telegramId;
    private UUID universityId; // null for SUPER_ADMIN
    private AdminRole role;
    private Instant createdAt;

    /**
     * Business rule: can this admin manage content for the given university?
     */
    public boolean canManageUniversity(UUID targetUniversityId) {
        if (role == AdminRole.SUPER_ADMIN) return true;
        return universityId != null && universityId.equals(targetUniversityId);
    }

    public boolean isSuperAdmin() {
        return role == AdminRole.SUPER_ADMIN;
    }
}
