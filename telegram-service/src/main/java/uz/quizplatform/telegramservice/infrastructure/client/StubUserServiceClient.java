package uz.quizplatform.telegramservice.infrastructure.client;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class StubUserServiceClient implements UserServiceClient {
    @Override
    public void registerOrUpdate(org.telegram.telegrambots.meta.api.objects.User from) {
        // TODO: Implement actual call
    }

    @Override
    public Optional<User> getUser(Long userId) {
        // TODO: Implement actual call
        return Optional.empty();
    }

    @Override
    public Object getUniversities() {
        // TODO: Implement actual call
        return null;
    }
}
