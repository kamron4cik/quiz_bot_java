package uz.quizplatform.userservice.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReviewPaymentRequestDto {

    @NotNull
    private UUID paymentRequestId;

    @NotNull
    private Long adminId;

    @NotNull
    private Boolean approve;
}
