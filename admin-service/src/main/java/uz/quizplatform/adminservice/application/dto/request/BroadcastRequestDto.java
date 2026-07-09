package uz.quizplatform.adminservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BroadcastRequestDto {

    @NotBlank
    private String message;

    private String parseMode = "HTML";

    private List<Long> targetUserIds;

    @NotNull
    private Long adminId;
}
