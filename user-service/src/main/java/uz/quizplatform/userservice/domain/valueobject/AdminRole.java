package uz.quizplatform.userservice.domain.valueobject;

/**
 * Represents the different types of administrative roles in the system.
 * SUPER_ADMIN can manage all universities.
 * ADMIN is scoped to a single university.
 * MODERATOR can approve payments and manage content but not users.
 */
public enum AdminRole {
    SUPER_ADMIN,
    ADMIN,
    MODERATOR
}
