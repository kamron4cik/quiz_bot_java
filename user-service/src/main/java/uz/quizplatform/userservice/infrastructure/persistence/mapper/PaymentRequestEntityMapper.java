package uz.quizplatform.userservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.quizplatform.userservice.domain.entity.PaymentRequest;
import uz.quizplatform.userservice.infrastructure.persistence.entity.PaymentRequestJpaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentRequestEntityMapper {
    PaymentRequest toDomain(PaymentRequestJpaEntity jpaEntity);
    PaymentRequestJpaEntity toJpaEntity(PaymentRequest domain);
}
