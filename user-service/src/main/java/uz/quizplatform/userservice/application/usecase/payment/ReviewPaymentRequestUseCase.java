package uz.quizplatform.userservice.application.usecase.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.AccessDeniedException;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.userservice.application.dto.request.ReviewPaymentRequestDto;
import uz.quizplatform.userservice.application.usecase.ManageUserAccessUseCase;
import uz.quizplatform.userservice.domain.entity.PaymentRequest;
import uz.quizplatform.userservice.domain.repository.AdminRepository;
import uz.quizplatform.userservice.domain.repository.PaymentRequestRepository;
import uz.quizplatform.userservice.infrastructure.messaging.PaymentEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewPaymentRequestUseCase {

    private final PaymentRequestRepository paymentRequestRepository;
    private final AdminRepository adminRepository;
    private final ManageUserAccessUseCase manageUserAccessUseCase;
    private final PaymentEventPublisher eventPublisher;

    public void execute(ReviewPaymentRequestDto requestDto) {
        var payment = paymentRequestRepository.findById(requestDto.getPaymentRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", requestDto.getPaymentRequestId()));

        var admin = adminRepository.findByTelegramId(requestDto.getAdminId())
                .orElseThrow(() -> new AccessDeniedException("User is not an admin"));

        if (!admin.canManageUniversity(payment.getUniversityId())) {
            throw new AccessDeniedException("Admin cannot manage payments for this university");
        }

        if (requestDto.getApprove()) {
            payment.approve(admin.getTelegramId());
            manageUserAccessUseCase.grantAccess(payment.getUserId());
        } else {
            payment.reject(admin.getTelegramId());
        }

        paymentRequestRepository.save(payment);

        eventPublisher.publishPaymentStatusChanged(payment);
        log.info("Payment request {} was {} by admin {}", payment.getId(), payment.getStatus(), admin.getTelegramId());
    }
}
