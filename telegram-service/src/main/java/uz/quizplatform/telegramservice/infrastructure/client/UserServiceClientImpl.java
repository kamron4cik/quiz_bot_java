package uz.quizplatform.telegramservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Primary
public class UserServiceClientImpl implements UserServiceClient {

    private final RestClient userServiceClientInstance;

    public UserServiceClientImpl(
            @org.springframework.beans.factory.annotation.Qualifier("userServiceClientInstance") RestClient userServiceClientInstance) {
        this.userServiceClientInstance = userServiceClientInstance;
    }

    @Override
    public void registerOrUpdate(org.telegram.telegrambots.meta.api.objects.User from) {
        log.info("Registering/updating Telegram user: id={}, username={}", from.getId(), from.getUserName());
        try {
            RegisterRequest req = new RegisterRequest(
                    from.getId(),
                    from.getUserName(),
                    from.getFirstName(),
                    from.getLastName()
            );
            userServiceClientInstance.post()
                    .uri("/api/v1/users/register")
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to register/update user in user-service: {}", e.getMessage());
        }
    }

    @Override
    public Optional<User> getUser(Long userId) {
        log.debug("Fetching user profile for: {}", userId);
        try {
            User user = userServiceClientInstance.get()
                    .uri("/api/v1/users/{id}", userId)
                    .retrieve()
                    .body(User.class);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Failed to fetch user profile for {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<UniversityDto> getUniversities() {
        log.debug("Fetching universities from user-service");
        try {
            return userServiceClientInstance.get()
                    .uri("/api/v1/users/universities")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UniversityDto>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch universities: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating academic profile for user {}", userId);
        try {
            userServiceClientInstance.patch()
                    .uri("/api/v1/users/{id}/profile", userId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to update user profile: {}", e.getMessage());
        }
    }

    @Override
    public void resetProfile(Long userId) {
        log.info("Resetting profile for user {}", userId);
        try {
            userServiceClientInstance.delete()
                    .uri("/api/v1/users/{id}/profile", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to reset user profile: {}", e.getMessage());
        }
    }

    @Override
    public void submitPayment(Long userId, String receiptFileId) {
        log.info("Submitting payment request for user {} with receipt file ID {}", userId, receiptFileId);
        try {
            SubmitPaymentDto req = new SubmitPaymentDto(userId, 15000, receiptFileId, "");
            userServiceClientInstance.post()
                    .uri("/api/v1/payments/submit")
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to submit payment request: {}", e.getMessage());
        }
    }

    private record SubmitPaymentDto(Long userId, int amount, String receiptFileId, String receiptStoragePath) {}
    private record RegisterRequest(
            Long telegramId,
            String username,
            String firstName,
            String lastName
    ) {}
}
