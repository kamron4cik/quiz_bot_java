package uz.quizplatform.userservice.application.usecase.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.BusinessRuleViolationException;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.userservice.application.dto.request.SubmitPaymentRequestDto;
import uz.quizplatform.userservice.domain.entity.PaymentRequest;
import uz.quizplatform.userservice.domain.repository.PaymentRequestRepository;
import uz.quizplatform.userservice.domain.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubmitPaymentRequestUseCase {

    private final UserRepository userRepository;
    private final PaymentRequestRepository paymentRequestRepository;

    public PaymentRequest execute(SubmitPaymentRequestDto requestDto) {
        var user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", requestDto.getUserId()));

        if (!user.isProfileComplete()) {
            throw new BusinessRuleViolationException("User profile is incomplete", "PROFILE_INCOMPLETE");
        }

        if (user.isHasPaid()) {
            throw new BusinessRuleViolationException("User has already paid", "ALREADY_PAID");
        }

        var paymentRequest = PaymentRequest.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .universityId(user.getUniversityId())
                .major(user.getMajor())
                .grade(user.getGrade())
                .studyMethod(user.getStudyMethod() != null ? user.getStudyMethod().name() : null)
                .testType(user.getTestType() != null ? user.getTestType().name() : null)
                .amount(requestDto.getAmount())
                .receiptFileId(requestDto.getReceiptFileId())
                .receiptStoragePath(requestDto.getReceiptStoragePath())
                .createdAt(Instant.now())
                .build();

        return paymentRequestRepository.save(paymentRequest);
    }
}
