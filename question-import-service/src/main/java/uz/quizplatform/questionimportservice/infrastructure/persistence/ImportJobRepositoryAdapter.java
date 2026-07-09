package uz.quizplatform.questionimportservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.questionimportservice.domain.entity.ImportJob;
import uz.quizplatform.questionimportservice.domain.repository.ImportJobRepository;
import uz.quizplatform.questionimportservice.infrastructure.persistence.mapper.ImportJobEntityMapper;
import uz.quizplatform.questionimportservice.infrastructure.persistence.repository.ImportJobJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ImportJobRepositoryAdapter implements ImportJobRepository {

    private final ImportJobJpaRepository jpaRepository;
    private final ImportJobEntityMapper mapper;

    @Override
    public ImportJob save(ImportJob job) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(job)));
    }

    @Override
    public Optional<ImportJob> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
