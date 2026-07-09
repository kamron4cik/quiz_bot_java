package uz.quizplatform.userservice.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * Request to register or update a user from Telegram's /start command.
 * This is an upsert: creates the user if not exists, updates activity if exists.
 */
@Data
@Builder
public class RegisterUserRequest {

    @NotNull(message = "Telegram ID is required")
    private Long telegramId;

    private String username;

    private String firstName;

    private String lastName;
}
