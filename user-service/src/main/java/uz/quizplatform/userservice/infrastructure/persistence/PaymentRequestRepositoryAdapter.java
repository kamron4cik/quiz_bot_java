package uz.quizplatform.userservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.userservice.domain.entity.PaymentRequest;
import uz.quizplatform.userservice.domain.repository.PaymentRequestRepository;
import uz.quizplatform.userservice.domain.valueobject.PaymentStatus;
import uz.quizplatform.userservice.infrastructure.persistence.mapper.PaymentRequestEntityMapper;
import uz.quizplatform.userservice.infrastructure.persistence.repository.PaymentRequestJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRequestRepositoryAdapter implements PaymentRequestRepository {

    private final PaymentRequestJpaRepository jpaRepository;
    private final PaymentRequestEntityMapper mapper;

    @Override
    public PaymentRequest save(PaymentRequest request) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(request)));
    }

    @Override
    public Optional<PaymentRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PaymentRequest> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PaymentRequest> findByStatusAndUniversityId(PaymentStatus status, UUID universityId) {
        if (universityId == null) {
             return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).toList();
        }
        return jpaRepository.findByStatusAndUniversityId(status, universityId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PaymentRequest> findPendingRequests(UUID universityId) {
        return findByStatusAndUniversityId(PaymentStatus.PENDING, universityId);
    }
}
