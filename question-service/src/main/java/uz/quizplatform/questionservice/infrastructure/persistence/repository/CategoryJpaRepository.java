package uz.quizplatform.questionservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.quizplatform.questionservice.infrastructure.persistence.entity.CategoryJpaEntity;

import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    List<CategoryJpaEntity> findByUniversityIdAndActiveTrue(UUID universityId);

    List<CategoryJpaEntity> findByActiveTrue();

    boolean existsByUniversityIdAndName(UUID universityId, String name);
}
