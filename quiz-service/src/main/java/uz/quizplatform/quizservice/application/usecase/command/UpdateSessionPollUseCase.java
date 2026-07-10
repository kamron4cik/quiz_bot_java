package uz.quizplatform.quizservice.application.usecase.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateSessionPollUseCase {

    private final QuizSessionRepository sessionRepository;
    private final QuizSessionCache sessionCache;

    public void execute(UUID sessionId, String pollId, List<String> shuffledOptions, int correctOptionIndex, Long messageId) {
        log.info("Updating session {} with pollId={}, messageId={}", sessionId, pollId, messageId);
        
        QuizSession session = sessionRepository.findAllActive().stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", sessionId));

        session.markQuestionSent(pollId, shuffledOptions, correctOptionIndex, messageId);
        sessionRepository.save(session);
        sessionCache.put(session.getUserId(), session);
    }
}
