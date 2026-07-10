package uz.quizplatform.quizservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    private final RestClient userServiceClientInstance;

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
