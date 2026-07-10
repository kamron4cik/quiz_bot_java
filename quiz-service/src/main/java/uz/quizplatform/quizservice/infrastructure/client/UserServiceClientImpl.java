package uz.quizplatform.quizservice.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Service
public class UserServiceClientImpl implements UserServiceClient {

    private final RestClient userServiceClientInstance;

    public UserServiceClientImpl(@Qualifier("userServiceClientInstance") RestClient userServiceClientInstance) {
        this.userServiceClientInstance = userServiceClientInstance;
    }

    @Override
    public Optional<User> getUser(Long userId) {
        log.debug("Calling user-service to get user profile: {}", userId);
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
}
