package uz.quizplatform.questionimportservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.quizplatform.questionimportservice.domain.valueobject.ImportJobStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "import_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJobJpaEntity {

    @Id
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "university_id", nullable = false)
    private UUID universityId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_format")
    private String fileFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImportJobStatus status;

    @Column(name = "detected_count")
    private int detectedCount;

    @Column(name = "valid_count")
    private int validCount;

    @Column(name = "duplicate_count")
    private int duplicateCount;

    @Column(name = "error_count")
    private int errorCount;

    @Column(name = "imported_count")
    private int importedCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = ImportJobStatus.UPLOADED;
    }
}
