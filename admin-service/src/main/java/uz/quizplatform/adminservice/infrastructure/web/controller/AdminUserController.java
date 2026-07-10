package uz.quizplatform.adminservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin Users", description = "Administrative endpoints to query and manage platform users")
public class AdminUserController {

    private final RestClient userServiceClient;

    public AdminUserController(
            @org.springframework.beans.factory.annotation.Qualifier("userServiceClient") RestClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @GetMapping
    @Operation(summary = "List all platform users (paginated)")
    public ResponseEntity<List<Object>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Object> users = userServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/users")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Object>>() {});
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search user by Telegram ID or username")
    public ResponseEntity<Object> searchUser(@RequestParam String q) {
        Object user = userServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/users/search")
                        .queryParam("q", q)
                        .build())
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/access/grant")
    @Operation(summary = "Manually grant platform access to user (approves payment manually)")
    public ResponseEntity<Void> grantAccess(@PathVariable Long id) {
        userServiceClient.post()
                .uri("/api/v1/users/{id}/access/grant", id)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.noContent().build();
    }
}
