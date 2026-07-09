package uz.quizplatform.questionimportservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateImportJobRequest {
    @NotNull
    private Long adminId;

    @NotNull
    private UUID universityId;

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String filePath;

    private String originalFilename;

    @NotBlank
    private String fileFormat;
}
