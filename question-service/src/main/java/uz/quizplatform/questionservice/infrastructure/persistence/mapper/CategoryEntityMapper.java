package uz.quizplatform.questionservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.quizplatform.questionservice.domain.entity.Category;
import uz.quizplatform.questionservice.infrastructure.persistence.entity.CategoryJpaEntity;

@Mapper(componentModel = "spring")
public interface CategoryEntityMapper {

    @Mapping(target = "active", source = "active")
    Category toDomain(CategoryJpaEntity entity);

    @Mapping(target = "active", source = "active")
    CategoryJpaEntity toEntity(Category domain);
}
