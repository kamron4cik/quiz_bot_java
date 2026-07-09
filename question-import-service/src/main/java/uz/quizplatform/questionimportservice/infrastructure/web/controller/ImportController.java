package uz.quizplatform.questionimportservice.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.questionimportservice.application.dto.CreateImportJobRequest;
import uz.quizplatform.questionimportservice.application.usecase.ParseImportJobUseCase;
import uz.quizplatform.questionimportservice.domain.entity.ImportJob;
import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;
import uz.quizplatform.questionimportservice.domain.repository.ImportJobRepository;
import uz.quizplatform.questionimportservice.domain.repository.ParsedQuestionRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportJobRepository importJobRepository;
    private final ParsedQuestionRepository parsedQuestionRepository;
    private final ParseImportJobUseCase parseImportJobUseCase;

    @PostMapping("/jobs")
    public ResponseEntity<ImportJob> createJob(@Valid @RequestBody CreateImportJobRequest request) {
        ImportJob job = ImportJob.builder()
                .id(UUID.randomUUID())
                .adminId(request.getAdminId())
                .universityId(request.getUniversityId())
                .categoryId(request.getCategoryId())
                .filePath(request.getFilePath())
                .originalFilename(request.getOriginalFilename())
                .fileFormat(request.getFileFormat())
                .createdAt(Instant.now())
                .build();

        ImportJob saved = importJobRepository.save(job);
        
        // Trigger async parsing
        parseImportJobUseCase.execute(saved.getId());

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ImportJob> getJobStatus(@PathVariable UUID jobId) {
        return importJobRepository.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs/{jobId}/preview")
    public ResponseEntity<List<ParsedQuestion>> getJobPreview(@PathVariable UUID jobId) {
        return ResponseEntity.ok(parsedQuestionRepository.findByJobId(jobId));
    }
}
