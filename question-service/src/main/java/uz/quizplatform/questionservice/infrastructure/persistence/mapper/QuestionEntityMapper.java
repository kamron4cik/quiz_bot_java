package uz.quizplatform.questionservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.quizplatform.questionservice.domain.entity.Question;
import uz.quizplatform.questionservice.infrastructure.persistence.entity.QuestionJpaEntity;

@Mapper(componentModel = "spring")
public interface QuestionEntityMapper {

    Question toDomain(QuestionJpaEntity entity);

    QuestionJpaEntity toEntity(Question domain);
}
