package uz.quizplatform.questionservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.questionservice.application.dto.request.CreateCategoryRequest;
import uz.quizplatform.questionservice.application.dto.response.CategoryResponse;
import uz.quizplatform.questionservice.application.usecase.CategoryUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @GetMapping
    @Operation(summary = "Get categories by university", description = "Returns all active categories for a given university")
    public ResponseEntity<List<CategoryResponse>> getByUniversity(
            @RequestParam UUID universityId) {
        return ResponseEntity.ok(categoryUseCase.getCategoriesByUniversity(universityId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryUseCase.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new category (admin)")
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryUseCase.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a category (admin, soft delete)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        categoryUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
