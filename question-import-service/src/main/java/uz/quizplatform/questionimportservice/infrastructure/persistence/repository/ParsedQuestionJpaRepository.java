package uz.quizplatform.questionimportservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.quizplatform.questionimportservice.infrastructure.persistence.entity.ParsedQuestionJpaEntity;

import java.util.List;
import java.util.UUID;

public interface ParsedQuestionJpaRepository extends JpaRepository<ParsedQuestionJpaEntity, UUID> {
    List<ParsedQuestionJpaEntity> findByJobId(UUID jobId);
    void deleteByJobId(UUID jobId);
}
