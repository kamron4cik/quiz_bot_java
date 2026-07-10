package uz.quizplatform.questionservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.questionservice.application.dto.request.CreateQuestionRequest;
import uz.quizplatform.questionservice.application.dto.response.QuestionResponse;
import uz.quizplatform.questionservice.application.usecase.QuestionUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Question management endpoints")
public class QuestionController {

    private final QuestionUseCase questionUseCase;

    @GetMapping("/{id}")
    @Operation(summary = "Get a single question by ID (includes correct answer for quiz-service)")
    public ResponseEntity<QuestionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(questionUseCase.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get questions by category",
               description = "Pass full=true to get full question objects, or omit for ID-only list")
    public ResponseEntity<?> getByCategory(
            @RequestParam UUID categoryId,
            @RequestParam(defaultValue = "false") boolean full) {
        if (full) {
            return ResponseEntity.ok(questionUseCase.getQuestionsByCategory(categoryId));
        } else {
            List<UUID> ids = questionUseCase.getQuestionIdsByCategory(categoryId);
            return ResponseEntity.ok(ids);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new question (admin / import-service)")
    public ResponseEntity<QuestionResponse> create(@RequestBody @Valid CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionUseCase.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a question (admin)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
