package uz.quizplatform.questionservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.quizplatform.questionservice.infrastructure.persistence.entity.QuestionJpaEntity;

import java.util.List;
import java.util.UUID;

public interface QuestionJpaRepository extends JpaRepository<QuestionJpaEntity, UUID> {

    @Query("SELECT q.id FROM QuestionJpaEntity q WHERE q.categoryId = :categoryId AND q.active = true")
    List<UUID> findIdsByCategoryIdAndActiveTrue(UUID categoryId);

    List<QuestionJpaEntity> findByCategoryIdAndActiveTrue(UUID categoryId);

    long countByCategoryIdAndActiveTrue(UUID categoryId);
}
