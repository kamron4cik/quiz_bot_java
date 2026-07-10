package uz.quizplatform.adminservice.infrastructure.web.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/imports")
@Tag(name = "Admin Imports", description = "Administrative endpoints for question document parsing and imports")
public class AdminImportController {

    private final RestClient importServiceClient;

    public AdminImportController(@org.springframework.beans.factory.annotation.Qualifier("importServiceClient") RestClient importServiceClient) {
        this.importServiceClient = importServiceClient;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload raw document file to import storage (MinIO)")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        Object response = importServiceClient.post()
                .uri("/api/v1/imports/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Object.class);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs")
    @Operation(summary = "Trigger import/parsing job for uploaded document path")
    public ResponseEntity<Object> createImportJob(@RequestBody Object request) {
        Object response = importServiceClient.post()
                .uri("/api/v1/imports/jobs")
                .body(request)
                .retrieve()
                .body(Object.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get status of an import job")
    public ResponseEntity<Object> getJobStatus(@PathVariable UUID jobId) {
        Object response = importServiceClient.get()
                .uri("/api/v1/imports/jobs/{jobId}", jobId)
                .retrieve()
                .body(Object.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}/preview")
    @Operation(summary = "Get preview of successfully parsed questions")
    public ResponseEntity<Object> getJobPreview(@PathVariable UUID jobId) {
        Object response = importServiceClient.get()
                .uri("/api/v1/imports/jobs/{jobId}/preview", jobId)
                .retrieve()
                .body(Object.class);

        return ResponseEntity.ok(response);
    }
}
