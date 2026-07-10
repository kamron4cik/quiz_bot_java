package uz.quizplatform.adminservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin Categories", description = "Administrative CRUD endpoints for quiz categories")
public class AdminCategoryController {

    private final RestClient questionServiceClient;

    @GetMapping
    @Operation(summary = "Get categories list by university")
    public ResponseEntity<List<Object>> getCategoriesByUniversity(@RequestParam UUID universityId) {
        List<Object> categories = questionServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/categories")
                        .queryParam("universityId", universityId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Object>>() {});
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @Operation(summary = "Create a new quiz category")
    public ResponseEntity<Object> createCategory(@RequestBody Object request) {
        Object category = questionServiceClient.post()
                .uri("/api/v1/categories")
                .body(request)
                .retrieve()
                .body(Object.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete/deactivate a category")
    public ResponseEntity<Void> deactivateCategory(@PathVariable UUID id) {
        questionServiceClient.delete()
                .uri("/api/v1/categories/{id}", id)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.noContent().build();
    }
}
