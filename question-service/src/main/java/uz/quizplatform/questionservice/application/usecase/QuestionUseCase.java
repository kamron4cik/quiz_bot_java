package uz.quizplatform.questionservice.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.questionservice.application.dto.request.CreateQuestionRequest;
import uz.quizplatform.questionservice.application.dto.response.QuestionResponse;
import uz.quizplatform.questionservice.domain.entity.Question;
import uz.quizplatform.questionservice.domain.repository.CategoryRepository;
import uz.quizplatform.questionservice.domain.repository.QuestionRepository;

import java.util.List;
import java.util.UUID;

/**
 * Use case for all question operations.
 *
 * Questions are the core business object of the platform.
 * This use case validates that the target category exists before
 * creating questions, and exposes ID-only and full question fetches
 * to support both the quiz-service (needs IDs for session shuffling)
 * and the notification-service (needs full question for poll delivery).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionUseCase {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public QuestionResponse getById(UUID id) {
        return questionRepository.findById(id)
                .map(QuestionResponse::from)
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<UUID> getQuestionIdsByCategory(UUID categoryId) {
        return questionRepository.findIdsByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByCategory(UUID categoryId) {
        return questionRepository.findByCategoryId(categoryId)
                .stream()
                .map(QuestionResponse::from)
                .toList();
    }

    @Transactional
    public QuestionResponse create(CreateQuestionRequest request) {
        // Validate category exists
        categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.categoryId()));

        var question = Question.create(
                request.categoryId(),
                request.text(),
                request.optionA(), request.optionB(),
                request.optionC(), request.optionD(),
                request.correctAnswer(),
                request.explanation(),
                request.imageUrl(),
                request.difficulty()
        );
        var saved = questionRepository.save(question);
        log.info("Created question {} in category {}", saved.getId(), saved.getCategoryId());
        return QuestionResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        var question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
        question.softDelete();
        questionRepository.save(question);
        log.info("Soft-deleted question {}", id);
    }
}
