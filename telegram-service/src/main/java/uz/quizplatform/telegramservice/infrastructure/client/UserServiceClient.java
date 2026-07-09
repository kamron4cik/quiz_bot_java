package uz.quizplatform.telegramservice.infrastructure.client;

import java.util.Optional;
import lombok.Getter;

public interface UserServiceClient {
    void registerOrUpdate(org.telegram.telegrambots.meta.api.objects.User from);
    Optional<User> getUser(Long userId);
    Object getUniversities();

    @Getter
    class User {
        private String firstName;
        private boolean admin;
        private boolean hasPaid;
        private boolean profileComplete;
    }
}
