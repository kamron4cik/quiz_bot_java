package uz.quizplatform.userservice.domain.repository;

import uz.quizplatform.userservice.domain.entity.PaymentRequest;
import uz.quizplatform.userservice.domain.valueobject.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRequestRepository {
    PaymentRequest save(PaymentRequest request);
    Optional<PaymentRequest> findById(UUID id);
    List<PaymentRequest> findByUserId(Long userId);
    List<PaymentRequest> findByStatusAndUniversityId(PaymentStatus status, UUID universityId);
    List<PaymentRequest> findPendingRequests(UUID universityId);
}
