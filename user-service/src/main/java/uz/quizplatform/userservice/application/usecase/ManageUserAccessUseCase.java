package uz.quizplatform.userservice.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.userservice.domain.entity.User;
import uz.quizplatform.userservice.domain.repository.UserRepository;

/**
 * Use Case: Grant or revoke quiz access.
 * Called by admin-service after payment approval/rejection events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManageUserAccessUseCase {

    private final UserRepository userRepository;

    public void grantAccess(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.grantAccess();
        userRepository.save(user);
        log.info("Access granted to user: {}", userId);
    }

    public void revokeAccess(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.revokeAccess();
        userRepository.save(user);
        log.info("Access revoked from user: {}", userId);
    }

    /**
     * Records quiz completion stats on the User aggregate.
     * Called when quiz-service publishes QuizSessionFinishedEvent.
     */
    public void recordQuizFinished(Long userId, int correctAnswers, int totalAnswered) {
        userRepository.findById(userId).ifPresent(user -> {
            user.recordQuizFinished(correctAnswers, totalAnswered);
            userRepository.save(user);
        });
    }
}
