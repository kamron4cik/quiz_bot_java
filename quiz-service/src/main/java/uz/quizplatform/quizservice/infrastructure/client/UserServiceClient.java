package uz.quizplatform.quizservice.infrastructure.client;

import java.util.Optional;
import java.util.UUID;
import lombok.Getter;

public interface UserServiceClient {
    Optional<User> getUser(Long userId);

    @Getter
    class User {
        private Long id;
        private UUID universityId;
        private boolean admin;
        private boolean hasPaid;
        private boolean profileComplete;
    }
}
