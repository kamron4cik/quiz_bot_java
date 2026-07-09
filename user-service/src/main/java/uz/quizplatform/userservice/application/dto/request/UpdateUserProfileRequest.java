package uz.quizplatform.userservice.application.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Request to update a user's academic profile.
 * Any field that is null will be left unchanged (partial update support).
 * This handles each step of the 5-step Telegram profile wizard.
 */
@Data
@Builder
public class UpdateUserProfileRequest {

    private UUID universityId;
    private String major;
    private Integer grade;
    private String studyMethod; // accepted as string code, converted to enum in use case
    private String testType;    // accepted as string code, converted to enum in use case
}
