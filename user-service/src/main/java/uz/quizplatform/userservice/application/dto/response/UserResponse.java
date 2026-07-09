package uz.quizplatform.userservice.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Full user response including profile and stats.
 * Returned to telegram-service for rendering profile summaries, stats pages, etc.
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String displayName;

    // Academic profile
    private UUID universityId;
    private String universityName;
    private String major;
    private Integer grade;
    private String studyMethod;
    private String testType;

    // Access
    private boolean hasPaid;
    private boolean profileComplete;
    private boolean isAdmin;

    // Stats
    private int totalTests;
    private int totalQuestions;
    private double averageScore;

    // Timestamps
    private Instant createdAt;
    private Instant lastActivity;
}
