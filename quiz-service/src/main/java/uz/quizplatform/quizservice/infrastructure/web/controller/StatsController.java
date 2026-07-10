package uz.quizplatform.quizservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.quizservice.application.service.UserQuizStatsService;
import uz.quizplatform.quizservice.infrastructure.persistence.entity.UserQuizStatsJpaEntity;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "User Stats", description = "Query resolved metrics for solved quizzes")
public class StatsController {

    private final UserQuizStatsService statsService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get detailed quiz solving stats for a single user")
    public ResponseEntity<UserQuizStatsJpaEntity> getUserStats(@PathVariable Long userId) {
        return statsService.getUserStats(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
