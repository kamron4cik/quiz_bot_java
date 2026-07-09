package uz.quizplatform.questionimportservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.quizplatform.questionimportservice.infrastructure.persistence.entity.ImportJobJpaEntity;

import java.util.UUID;

public interface ImportJobJpaRepository extends JpaRepository<ImportJobJpaEntity, UUID> {
}
