package uz.quizplatform.adminservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Stats", description = "Query platform-wide statistics for administrators")
public class AdminStatsController {

    private final RestClient userServiceClient;
    private final RestClient quizServiceClient;

    @GetMapping("/dashboard")
    @Operation(summary = "Get high-level counts for administrator dashboards")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        Long totalUsers = userServiceClient.get()
                .uri("/api/v1/users/count")
                .retrieve()
                .body(Long.class);

        return ResponseEntity.ok(new DashboardStats(
                totalUsers != null ? totalUsers : 0L
        ));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get global leaderboard list for admin reporting")
    public ResponseEntity<Object> getLeaderboard(@RequestParam(defaultValue = "20") int limit) {
        Object leaderboard = quizServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/leaderboard/global")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(leaderboard);
    }

    public record DashboardStats(long totalUsers) {}
}
