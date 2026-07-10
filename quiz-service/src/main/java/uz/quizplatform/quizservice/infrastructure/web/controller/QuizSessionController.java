package uz.quizplatform.quizservice.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.quizservice.application.dto.request.StartQuizRequest;
import uz.quizplatform.quizservice.application.dto.response.QuizSessionResponse;
import uz.quizplatform.quizservice.application.usecase.command.*;
import uz.quizplatform.quizservice.application.usecase.query.GetPausedSessionUseCase;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz Sessions", description = "Endpoints for managing quiz sessions")
public class QuizSessionController {

    private final StartQuizSessionUseCase startUseCase;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final UpdateSessionPollUseCase updateSessionPollUseCase;
    private final GetPausedSessionUseCase getPausedSessionUseCase;
    private final PauseSessionUseCase pauseSessionUseCase;
    private final ResumeSessionUseCase resumeSessionUseCase;

    @PostMapping("/start")
    @Operation(summary = "Start a new quiz session")
    public ResponseEntity<QuizSessionResponse> startSession(@RequestBody @Valid StartQuizRequest request) {
        return ResponseEntity.ok(startUseCase.execute(request));
    }

    @PostMapping("/answer")
    @Operation(summary = "Submit option choice for active Telegram poll")
    public ResponseEntity<SubmitAnswerUseCase.SubmitAnswerResponse> submitAnswer(
            @RequestParam String pollId,
            @RequestParam Long userId,
            @RequestParam int optionId) {
        return ResponseEntity.ok(submitAnswerUseCase.execute(pollId, userId, optionId));
    }

    @PutMapping("/sessions/{sessionId}/poll")
    @Operation(summary = "REST callback from notification-service to associate generated pollId & messageId")
    public ResponseEntity<Void> updateSessionPoll(
            @PathVariable UUID sessionId,
            @RequestBody @Valid UpdatePollRequestDto request) {
        updateSessionPollUseCase.execute(
                sessionId,
                request.pollId(),
                request.shuffledOptions(),
                request.correctOptionIndex(),
                request.messageId()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session/paused")
    @Operation(summary = "Get active paused session details for bot wizard resume triggers")
    public ResponseEntity<GetPausedSessionUseCase.PausedSessionResponse> getPausedSession(
            @RequestParam Long userId) {
        return getPausedSessionUseCase.execute(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pause")
    @Operation(summary = "Pause an active session")
    public ResponseEntity<Void> pauseSession(@RequestParam Long userId) {
        pauseSessionUseCase.execute(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resume")
    @Operation(summary = "Resume a paused session")
    public ResponseEntity<Void> resumeSession(@RequestParam Long userId) {
        resumeSessionUseCase.execute(userId);
        return ResponseEntity.ok().build();
    }

    public record UpdatePollRequestDto(
            String pollId,
            java.util.List<String> shuffledOptions,
            int correctOptionIndex,
            Long messageId
    ) {}
}
