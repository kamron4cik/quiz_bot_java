package uz.quizplatform.quizservice.domain.repository;

import java.util.Optional;
import java.util.UUID;
import lombok.Getter;

public interface CategoryRepository {
    Optional<Category> findById(UUID categoryId);
    
    @Getter
    class Category {
        private String name;
        private boolean active;
    }
}
