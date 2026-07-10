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
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
@Tag(name = "Admin Questions", description = "Administrative CRUD endpoints for question items")
public class AdminQuestionController {

    private final RestClient questionServiceClient;

    @GetMapping("/{id}")
    @Operation(summary = "Get single question details")
    public ResponseEntity<Object> getQuestionById(@PathVariable UUID id) {
        Object question = questionServiceClient.get()
                .uri("/api/v1/questions/{id}", id)
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(question);
    }

    @GetMapping
    @Operation(summary = "Get questions list by category")
    public ResponseEntity<List<Object>> getQuestionsByCategory(@RequestParam UUID categoryId) {
        List<Object> questions = questionServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/questions")
                        .queryParam("categoryId", categoryId)
                        .queryParam("full", true)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Object>>() {});
        return ResponseEntity.ok(questions);
    }

    @PostMapping
    @Operation(summary = "Create a new question manually")
    public ResponseEntity<Object> createQuestion(@RequestBody Object request) {
        Object question = questionServiceClient.post()
                .uri("/api/v1/questions")
                .body(request)
                .retrieve()
                .body(Object.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionServiceClient.delete()
                .uri("/api/v1/questions/{id}", id)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.noContent().build();
    }
}
