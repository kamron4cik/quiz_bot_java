package uz.quizplatform.userservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.quizplatform.userservice.domain.entity.Admin;
import uz.quizplatform.userservice.domain.entity.University;
import uz.quizplatform.userservice.infrastructure.persistence.entity.AdminJpaEntity;
import uz.quizplatform.userservice.infrastructure.persistence.entity.UniversityJpaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UniversityEntityMapper {
    University toDomain(UniversityJpaEntity jpaEntity);
    UniversityJpaEntity toJpaEntity(University domain);
    Admin toDomain(AdminJpaEntity jpaEntity);
    AdminJpaEntity toJpaEntity(Admin domain);
}
