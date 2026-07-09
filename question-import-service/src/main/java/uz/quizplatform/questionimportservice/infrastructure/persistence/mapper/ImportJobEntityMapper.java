package uz.quizplatform.questionimportservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.quizplatform.questionimportservice.domain.entity.ImportJob;
import uz.quizplatform.questionimportservice.infrastructure.persistence.entity.ImportJobJpaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImportJobEntityMapper {
    ImportJob toDomain(ImportJobJpaEntity jpaEntity);
    ImportJobJpaEntity toJpaEntity(ImportJob domain);
}
