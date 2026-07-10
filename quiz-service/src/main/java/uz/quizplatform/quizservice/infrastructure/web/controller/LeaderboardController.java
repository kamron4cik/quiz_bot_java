package uz.quizplatform.quizservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.quizservice.application.service.UserQuizStatsService;
import uz.quizplatform.quizservice.infrastructure.persistence.entity.UserQuizStatsJpaEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboards", description = "High-performance rankings global and by university")
public class LeaderboardController {

    private final UserQuizStatsService statsService;

    @GetMapping("/global")
    @Operation(summary = "Get global leaderboard top list")
    public ResponseEntity<List<UserQuizStatsJpaEntity>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getGlobalLeaderboard(limit));
    }

    @GetMapping("/university/{universityId}")
    @Operation(summary = "Get leaderboard filtered by university")
    public ResponseEntity<List<UserQuizStatsJpaEntity>> getUniversityLeaderboard(
            @PathVariable UUID universityId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getUniversityLeaderboard(universityId, limit));
    }

    @GetMapping("/rank/{userId}")
    @Operation(summary = "Get user global numeric rank")
    public ResponseEntity<Long> getGlobalRank(@PathVariable Long userId) {
        return ResponseEntity.ok(statsService.getUserRank(userId));
    }

    @GetMapping("/rank/{userId}/university/{universityId}")
    @Operation(summary = "Get user rank relative to their university peers")
    public ResponseEntity<Long> getUniversityRank(
            @PathVariable Long userId,
            @PathVariable UUID universityId) {
        return ResponseEntity.ok(statsService.getUserRankInUniversity(userId, universityId));
    }
}
