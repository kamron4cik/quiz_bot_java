package uz.quizplatform.questionimportservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.quizplatform.questionimportservice.domain.valueobject.ImportJobStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJob {

    private UUID id;
    private Long adminId;
    private UUID universityId;
    private UUID categoryId;

    // Source file info
    private String filePath;
    private String originalFilename;
    private String fileFormat;

    @Builder.Default
    private ImportJobStatus status = ImportJobStatus.UPLOADED;

    // Parse results
    @Builder.Default
    private int detectedCount = 0;
    @Builder.Default
    private int validCount = 0;
    @Builder.Default
    private int duplicateCount = 0;
    @Builder.Default
    private int errorCount = 0;
    @Builder.Default
    private int importedCount = 0;

    private String errorMessage;

    private Instant createdAt;
    private Instant completedAt;

    public void markAsParsing() {
        this.status = ImportJobStatus.PARSING;
    }

    public void markAsPreviewReady(int detected, int valid, int duplicate, int error) {
        this.status = ImportJobStatus.PREVIEW_READY;
        this.detectedCount = detected;
        this.validCount = valid;
        this.duplicateCount = duplicate;
        this.errorCount = error;
    }

    public void markAsConfirmed() {
        if (this.status != ImportJobStatus.PREVIEW_READY) {
            throw new IllegalStateException("Job must be PREVIEW_READY to confirm");
        }
        this.status = ImportJobStatus.CONFIRMED;
    }

    public void markAsImporting() {
        this.status = ImportJobStatus.IMPORTING;
    }

    public void markAsCompleted(int imported) {
        this.status = ImportJobStatus.COMPLETED;
        this.importedCount = imported;
        this.completedAt = Instant.now();
    }

    public void markAsFailed(String errorMsg) {
        this.status = ImportJobStatus.FAILED;
        this.errorMessage = errorMsg;
        this.completedAt = Instant.now();
    }

    public void markAsCancelled() {
        this.status = ImportJobStatus.CANCELLED;
        this.completedAt = Instant.now();
    }
}
