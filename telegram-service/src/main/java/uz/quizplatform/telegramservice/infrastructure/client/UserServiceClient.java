package uz.quizplatform.telegramservice.infrastructure.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public interface UserServiceClient {
    void registerOrUpdate(org.telegram.telegrambots.meta.api.objects.User from);
    Optional<User> getUser(Long userId);
    List<UniversityDto> getUniversities();
    
    void updateProfile(Long userId, UpdateProfileRequest request);
    void resetProfile(Long userId);
    void submitPayment(Long userId, String receiptFileId);

    @Getter
    @Setter
    class User {
        private Long id;
        private String firstName;
        private boolean admin;
        private boolean hasPaid;
        private boolean profileComplete;
        private UUID universityId;
    }

    record UniversityDto(UUID id, String name) {}
    record UpdateProfileRequest(
            UUID universityId,
            String major,
            Integer grade,
            String studyMethod,
            String testType
    ) {}
}
