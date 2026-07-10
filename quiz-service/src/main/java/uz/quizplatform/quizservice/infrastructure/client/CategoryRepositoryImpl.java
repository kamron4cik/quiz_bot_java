package uz.quizplatform.quizservice.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.quizplatform.quizservice.domain.repository.CategoryRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CategoryRepositoryImpl implements CategoryRepository {

    private final RestClient questionServiceClientInstance;

    public CategoryRepositoryImpl(@Qualifier("questionServiceClientInstance") RestClient questionServiceClientInstance) {
        this.questionServiceClientInstance = questionServiceClientInstance;
    }

    @Override
    public Optional<Category> findById(UUID categoryId) {
        log.debug("Calling question-service to get category: {}", categoryId);
        try {
            Category category = questionServiceClientInstance.get()
                    .uri("/api/v1/categories/{id}", categoryId)
                    .retrieve()
                    .body(Category.class);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            log.error("Failed to fetch category {}: {}", categoryId, e.getMessage());
            return Optional.empty();
        }
    }
}
