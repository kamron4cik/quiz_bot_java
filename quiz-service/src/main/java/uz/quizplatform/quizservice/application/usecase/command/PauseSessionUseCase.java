package uz.quizplatform.quizservice.application.usecase.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PauseSessionUseCase {

    private final QuizSessionRepository sessionRepository;
    private final QuizSessionCache sessionCache;

    public void execute(Long userId) {
        log.info("Pausing active quiz session for user {}", userId);

        QuizSession session = sessionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active QuizSession for user", userId));

        session.pause();
        sessionRepository.save(session);
        sessionCache.delete(userId); // remove from active cache so scheduler ignores it
    }
}
