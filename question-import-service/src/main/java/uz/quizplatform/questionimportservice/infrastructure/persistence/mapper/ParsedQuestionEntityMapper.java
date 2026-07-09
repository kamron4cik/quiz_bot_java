package uz.quizplatform.questionimportservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;
import uz.quizplatform.questionimportservice.infrastructure.persistence.entity.ParsedQuestionJpaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParsedQuestionEntityMapper {
    ParsedQuestion toDomain(ParsedQuestionJpaEntity jpaEntity);
    ParsedQuestionJpaEntity toJpaEntity(ParsedQuestion domain);
}
