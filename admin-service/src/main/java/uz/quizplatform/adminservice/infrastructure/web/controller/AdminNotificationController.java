package uz.quizplatform.adminservice.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.quizplatform.adminservice.application.dto.request.BroadcastRequestDto;
import uz.quizplatform.adminservice.infrastructure.messaging.NotificationEventPublisher;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationEventPublisher eventPublisher;

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(@Valid @RequestBody BroadcastRequestDto request) {
        eventPublisher.publishBroadcast(
                request.getMessage(),
                request.getParseMode(),
                request.getTargetUserIds(),
                request.getAdminId()
        );
        return ResponseEntity.accepted().build();
    }
}
