package uz.quizplatform.questionservice.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.questionservice.application.dto.request.CreateCategoryRequest;
import uz.quizplatform.questionservice.application.dto.response.CategoryResponse;
import uz.quizplatform.questionservice.domain.entity.Category;
import uz.quizplatform.questionservice.domain.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;

/**
 * Use case for all category operations.
 *
 * Categories are the primary filter for quiz questions:
 * a user's university determines which categories are available.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByUniversity(UUID universityId) {
        return categoryRepository.findByUniversityId(universityId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::from)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByUniversityIdAndName(request.universityId(), request.name())) {
            throw new IllegalArgumentException(
                    "Category '" + request.name() + "' already exists for this university");
        }
        var category = Category.create(request.universityId(), request.name(), request.description());
        var saved = categoryRepository.save(category);
        log.info("Created category '{}' for university {}", saved.getName(), saved.getUniversityId());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public void deactivate(UUID id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        category.deactivate();
        categoryRepository.save(category);
        log.info("Deactivated category {}", id);
    }
}
