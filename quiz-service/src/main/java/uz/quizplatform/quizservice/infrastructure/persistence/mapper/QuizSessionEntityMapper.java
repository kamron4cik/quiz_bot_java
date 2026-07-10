package uz.quizplatform.quizservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.infrastructure.persistence.entity.QuizSessionJpaEntity;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface QuizSessionEntityMapper {

    @Mapping(target = "score", expression = "java(entity.getScore().doubleValue())")
    QuizSession toDomain(QuizSessionJpaEntity entity);

    @Mapping(target = "score", expression = "java(java.math.BigDecimal.valueOf(domain.getScore()))")
    QuizSessionJpaEntity toEntity(QuizSession domain);
}
