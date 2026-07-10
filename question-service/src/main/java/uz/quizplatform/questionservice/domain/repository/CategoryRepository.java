package uz.quizplatform.questionservice.domain.repository;

import uz.quizplatform.questionservice.domain.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findByUniversityId(UUID universityId);
    List<Category> findAllActive();
    boolean existsByUniversityIdAndName(UUID universityId, String name);
}
