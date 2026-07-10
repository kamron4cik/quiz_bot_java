package uz.quizplatform.quizservice.application.usecase.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.CategoryRepository;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPausedSessionUseCase {

    private final QuizSessionRepository sessionRepository;
    private final CategoryRepository categoryRepository;

    public Optional<PausedSessionResponse> execute(Long userId) {
        log.debug("Checking paused session for user {}", userId);
        return sessionRepository.findPausedByUserId(userId)
                .map(session -> {
                    String categoryName = categoryRepository.findById(session.getCategoryId())
                            .map(CategoryRepository.Category::getName)
                            .orElse("Kategoriya");
                    return new PausedSessionResponse(
                            categoryName,
                            session.getCurrentQuestionIndex(),
                            session.getQuestionCount()
                    );
                });
    }

    public record PausedSessionResponse(
            String categoryName,
            int currentQuestionIndex,
            int questionCount
    ) {}
}
