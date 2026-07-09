package uz.quizplatform.questionimportservice.domain.repository;

import uz.quizplatform.questionimportservice.domain.entity.ImportJob;

import java.util.Optional;
import java.util.UUID;

public interface ImportJobRepository {
    ImportJob save(ImportJob job);
    Optional<ImportJob> findById(UUID id);
}
