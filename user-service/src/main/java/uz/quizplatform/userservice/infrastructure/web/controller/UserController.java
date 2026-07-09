package uz.quizplatform.userservice.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.userservice.application.dto.request.RegisterUserRequest;
import uz.quizplatform.userservice.application.dto.request.UpdateUserProfileRequest;
import uz.quizplatform.userservice.application.dto.response.UserResponse;
import uz.quizplatform.userservice.application.usecase.ManageUserAccessUseCase;
import uz.quizplatform.userservice.application.usecase.RegisterOrUpdateUserUseCase;
import uz.quizplatform.userservice.application.usecase.UpdateUserProfileUseCase;
import uz.quizplatform.userservice.domain.entity.University;
import uz.quizplatform.userservice.domain.repository.UniversityRepository;
import uz.quizplatform.userservice.domain.repository.UserRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

/**
 * REST controller for user management.
 * All endpoints are consumed by: telegram-service, admin-service, quiz-service (inter-service calls).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterOrUpdateUserUseCase registerOrUpdateUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ManageUserAccessUseCase manageUserAccessUseCase;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;

    /**
     * Register or update a user (upsert).
     * Called by telegram-service on every /start command.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerOrUpdate(@Valid @RequestBody RegisterUserRequest request) {
        log.debug("POST /api/v1/users/register for telegramId={}", request.getTelegramId());
        return ResponseEntity.ok(registerOrUpdateUserUseCase.execute(request));
    }

    /**
     * Get full user profile including university, academic info, and stats.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    var uni = user.getUniversityId() != null
                            ? universityRepository.findById(user.getUniversityId()).orElse(null)
                            : null;
                    return ResponseEntity.ok(UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .displayName(user.getDisplayName())
                            .universityId(user.getUniversityId())
                            .universityName(uni != null ? uni.getName() : null)
                            .major(user.getMajor())
                            .grade(user.getGrade())
                            .studyMethod(user.getStudyMethod() != null ? user.getStudyMethod().getCode() : null)
                            .testType(user.getTestType() != null ? user.getTestType().getCode() : null)
                            .hasPaid(user.isHasPaid())
                            .profileComplete(user.isProfileComplete())
                            .totalTests(user.getTotalTests())
                            .totalQuestions(user.getTotalQuestions())
                            .averageScore(user.getAverageScore())
                            .createdAt(user.getCreatedAt())
                            .lastActivity(user.getLastActivity())
                            .build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update user's academic profile (supports partial update).
     * Called for each step of the 5-step Telegram wizard.
     */
    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(updateUserProfileUseCase.execute(id, request));
    }

    /**
     * Reset user's profile to empty state.
     * Called when user clicks "Qaytadan tanlash" (Reset profile).
     */
    @DeleteMapping("/{id}/profile")
    public ResponseEntity<UserResponse> resetProfile(@PathVariable Long id) {
        return ResponseEntity.ok(updateUserProfileUseCase.resetProfile(id));
    }

    /**
     * Get paginated user list (admin use).
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var users = userRepository.findAllPaginated(page, size).stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .displayName(user.getDisplayName())
                        .hasPaid(user.isHasPaid())
                        .universityId(user.getUniversityId())
                        .lastActivity(user.getLastActivity())
                        .build())
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * Get total user count (for pagination headers).
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(userRepository.count());
    }

    /**
     * Search user by Telegram ID or username.
     * Called by admin-service for user lookup.
     */
    @GetMapping("/search")
    public ResponseEntity<UserResponse> searchUser(@RequestParam String q) {
        var result = q.matches("\\d+")
                ? userRepository.findById(Long.parseLong(q))
                : userRepository.findByUsername(q.replace("@", "").trim());

        return result.map(user -> ResponseEntity.ok(UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .displayName(user.getDisplayName())
                        .universityId(user.getUniversityId())
                        .major(user.getMajor())
                        .grade(user.getGrade())
                        .studyMethod(user.getStudyMethod() != null ? user.getStudyMethod().getCode() : null)
                        .testType(user.getTestType() != null ? user.getTestType().getCode() : null)
                        .hasPaid(user.isHasPaid())
                        .profileComplete(user.isProfileComplete())
                        .totalTests(user.getTotalTests())
                        .totalQuestions(user.getTotalQuestions())
                        .averageScore(user.getAverageScore())
                        .createdAt(user.getCreatedAt())
                        .lastActivity(user.getLastActivity())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Export all users as CSV (admin use).
     * Streams response for large datasets.
     */
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv() {
        var users = userRepository.findAll();
        var universityCache = new java.util.HashMap<java.util.UUID, String>();
        universityRepository.findAll().forEach(u -> universityCache.put(u.getId(), u.getName()));

        var sb = new StringBuilder();
        sb.append("ID,Username,To'lov holati,Ism,Familiya,Universitet,Mutaxassislik,Kurs,Ta'lim shakli,Test turi,Jami testlar,Jami savollar,O'rtacha ball,Oxirgi faollik\n");
        for (var u : users) {
            sb.append(String.join(",",
                    quote(String.valueOf(u.getId())),
                    quote(u.getUsername() != null ? "@" + u.getUsername() : ""),
                    quote(u.isHasPaid() ? "✅ To'langan" : "❌ To'lanmagan"),
                    quote(u.getFirstName() != null ? u.getFirstName() : ""),
                    quote(u.getLastName() != null ? u.getLastName() : ""),
                    quote(u.getUniversityId() != null ? universityCache.getOrDefault(u.getUniversityId(), "") : ""),
                    quote(u.getMajor() != null ? u.getMajor() : ""),
                    quote(u.getGrade() != null ? u.getGrade() + "-kurs" : ""),
                    quote(u.getStudyMethod() != null ? u.getStudyMethod().getCode() : ""),
                    quote(u.getTestType() != null ? u.getTestType().getCode() : ""),
                    quote(String.valueOf(u.getTotalTests())),
                    quote(String.valueOf(u.getTotalQuestions())),
                    quote(String.valueOf(u.getAverageScore())),
                    quote(u.getLastActivity() != null ? u.getLastActivity().toString() : "")
            )).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users_" + Instant.now().toEpochMilli() + ".csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body("\uFEFF" + sb); // BOM for Excel UTF-8 compatibility
    }

    /**
     * Grant access after payment approval.
     */
    @PostMapping("/{id}/access/grant")
    public ResponseEntity<Void> grantAccess(@PathVariable Long id) {
        manageUserAccessUseCase.grantAccess(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Record quiz completion stats on user aggregate.
     */
    @PostMapping("/{id}/stats/quiz-finished")
    public ResponseEntity<Void> recordQuizFinished(
            @PathVariable Long id,
            @RequestParam int correctAnswers,
            @RequestParam int totalAnswered) {
        manageUserAccessUseCase.recordQuizFinished(id, correctAnswers, totalAnswered);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all universities (for profile wizard step 1).
     */
    @GetMapping("/universities")
    public ResponseEntity<List<University>> listUniversities() {
        return ResponseEntity.ok(universityRepository.findAll());
    }

    /**
     * Get currently active users (last N minutes).
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers(
            @RequestParam(defaultValue = "15") int minutes) {
        var since = Instant.now().minusSeconds((long) minutes * 60);
        var users = userRepository.findActiveAfter(since).stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .firstName(u.getFirstName())
                        .lastActivity(u.getLastActivity())
                        .build())
                .toList();
        return ResponseEntity.ok(users);
    }

    private String quote(String value) {
        return "\"" + (value != null ? value.replace("\"", "\"\"") : "") + "\"";
    }
}
