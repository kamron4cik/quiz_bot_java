package uz.quizplatform.userservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.quizplatform.userservice.infrastructure.persistence.entity.UniversityJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface UniversityJpaRepository extends JpaRepository<UniversityJpaEntity, UUID> {
    Optional<UniversityJpaEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
