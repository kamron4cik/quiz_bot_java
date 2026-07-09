package uz.quizplatform.userservice.domain.entity;

import lombok.*;
import uz.quizplatform.userservice.domain.valueobject.StudyMethod;
import uz.quizplatform.userservice.domain.valueobject.TestType;

import java.time.Instant;
import java.util.UUID;

/**
 * User domain entity — aggregate root.
 *
 * The user's primary key is their Telegram ID (Long).
 * This is intentional: Telegram IDs are unique, stable, and we never need
 * a separate surrogate key since users only ever log in via Telegram.
 *
 * Business rules:
 * - A new user starts with has_paid=false and incomplete profile
 * - Profile completion is required before accessing quizzes
 * - Admins bypass the profile check and payment requirement
 * - Payment approval grants has_paid=true (irreversible unless admin manually revokes)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Telegram User ID — serves as the primary key */
    private Long id;

    private String username;
    private String firstName;
    private String lastName;

    // Academic profile
    private UUID universityId;
    private String major;
    private Integer grade;           // 1-5 (university year)
    private StudyMethod studyMethod;
    private TestType testType;

    // Access control
    @Builder.Default
    private boolean hasPaid = false;

    // Timestamps
    private Instant createdAt;
    private Instant lastActivity;

    // Aggregate stats (denormalized for quick access)
    @Builder.Default
    private int totalTests = 0;
    @Builder.Default
    private int totalQuestions = 0;
    @Builder.Default
    private double averageScore = 0.0;

    /**
     * Factory method for creating a new user from Telegram data.
     * Always returns a user with incomplete profile and no payment.
     */
    public static User registerFromTelegram(Long telegramId, String username, String firstName, String lastName) {
        return User.builder()
                .id(telegramId)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .hasPaid(false)
                .createdAt(Instant.now())
                .lastActivity(Instant.now())
                .totalTests(0)
                .totalQuestions(0)
                .averageScore(0.0)
                .build();
    }

    /**
     * Business rule: A user's profile is complete when all 5 academic fields are set.
     * Incomplete profiles are blocked from accessing quiz content.
     */
    public boolean isProfileComplete() {
        return universityId != null
                && major != null && !major.isBlank()
                && grade != null && grade >= 1 && grade <= 5
                && studyMethod != null
                && testType != null;
    }

    /**
     * Business rule: A user can access quizzes if they have paid OR they are an admin.
     * Admin status is checked separately via the Admin entity.
     */
    public boolean canAccessQuizzes() {
        return hasPaid;
    }

    public void grantAccess() {
        this.hasPaid = true;
    }

    public void revokeAccess() {
        this.hasPaid = false;
    }

    public void updateActivity() {
        this.lastActivity = Instant.now();
    }

    public void updateProfile(UUID universityId, String major, Integer grade, StudyMethod studyMethod, TestType testType) {
        this.universityId = universityId;
        this.major = major;
        this.grade = grade;
        this.studyMethod = studyMethod;
        this.testType = testType;
    }

    public void resetProfile() {
        this.universityId = null;
        this.major = null;
        this.grade = null;
        this.studyMethod = null;
        this.testType = null;
    }

    /**
     * Updates aggregate quiz stats.
     * Called when a quiz session finishes (any terminal state: completed/stopped/timeout).
     */
    public void recordQuizFinished(int correctAnswers, int totalAnswered) {
        long previousCorrect = Math.round(this.averageScore / 100.0 * this.totalQuestions);
        long newTotalQuestions = this.totalQuestions + totalAnswered;
        long newCorrect = previousCorrect + correctAnswers;
        this.totalTests += 1;
        this.totalQuestions = (int) newTotalQuestions;
        this.averageScore = newTotalQuestions > 0
                ? Math.round((double) newCorrect / newTotalQuestions * 100.0 * 10) / 10.0
                : 0.0;
    }

    public String getDisplayName() {
        if (firstName != null && !firstName.isBlank()) {
            return lastName != null ? firstName + " " + lastName : firstName;
        }
        if (username != null) return "@" + username;
        return "User_" + id;
    }
}
