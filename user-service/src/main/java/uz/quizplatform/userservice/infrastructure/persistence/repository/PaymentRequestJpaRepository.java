package uz.quizplatform.userservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.quizplatform.userservice.domain.valueobject.PaymentStatus;
import uz.quizplatform.userservice.infrastructure.persistence.entity.PaymentRequestJpaEntity;

import java.util.List;
import java.util.UUID;

public interface PaymentRequestJpaRepository extends JpaRepository<PaymentRequestJpaEntity, UUID> {
    List<PaymentRequestJpaEntity> findByUserId(Long userId);
    List<PaymentRequestJpaEntity> findByStatusAndUniversityId(PaymentStatus status, UUID universityId);
    List<PaymentRequestJpaEntity> findByStatus(PaymentStatus status);
}
