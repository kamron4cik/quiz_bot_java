package uz.quizplatform.questionservice.domain.repository;

import uz.quizplatform.questionservice.domain.entity.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository {
    Question save(Question question);
    Optional<Question> findById(UUID id);
    List<UUID> findIdsByCategoryId(UUID categoryId);
    List<Question> findByCategoryId(UUID categoryId);
    long countByCategoryId(UUID categoryId);
}
