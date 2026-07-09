package uz.quizplatform.userservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.quizplatform.userservice.domain.entity.User;
import uz.quizplatform.userservice.infrastructure.persistence.entity.UserJpaEntity;

/**
 * MapStruct mapper between the domain User entity and the JPA persistence entity.
 * Intentionally separate to maintain the Clean Architecture boundary.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEntityMapper {

    User toDomain(UserJpaEntity jpaEntity);

    UserJpaEntity toJpaEntity(User domain);
}
