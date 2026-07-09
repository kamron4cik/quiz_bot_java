package uz.quizplatform.userservice.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.userservice.application.dto.request.ReviewPaymentRequestDto;
import uz.quizplatform.userservice.application.dto.request.SubmitPaymentRequestDto;
import uz.quizplatform.userservice.application.usecase.payment.ReviewPaymentRequestUseCase;
import uz.quizplatform.userservice.application.usecase.payment.SubmitPaymentRequestUseCase;
import uz.quizplatform.userservice.domain.entity.PaymentRequest;
import uz.quizplatform.userservice.domain.repository.PaymentRequestRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SubmitPaymentRequestUseCase submitPaymentUseCase;
    private final ReviewPaymentRequestUseCase reviewPaymentUseCase;
    private final PaymentRequestRepository paymentRequestRepository;

    @PostMapping("/submit")
    public ResponseEntity<PaymentRequest> submitPayment(@Valid @RequestBody SubmitPaymentRequestDto request) {
        return ResponseEntity.ok(submitPaymentUseCase.execute(request));
    }

    @PostMapping("/review")
    public ResponseEntity<Void> reviewPayment(@Valid @RequestBody ReviewPaymentRequestDto request) {
        reviewPaymentUseCase.execute(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentRequest>> getUserPayments(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentRequestRepository.findByUserId(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PaymentRequest>> getPendingPayments(
            @RequestParam(required = false) UUID universityId) {
        return ResponseEntity.ok(paymentRequestRepository.findPendingRequests(universityId));
    }
}
