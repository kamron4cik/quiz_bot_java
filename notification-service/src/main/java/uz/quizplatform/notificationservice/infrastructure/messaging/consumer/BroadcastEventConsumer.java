package uz.quizplatform.notificationservice.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.BroadcastMessageEvent;
import uz.quizplatform.notificationservice.infrastructure.config.RabbitMqConfig;
import uz.quizplatform.notificationservice.infrastructure.telegram.TelegramApiClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastEventConsumer {

    private final TelegramApiClient telegramApiClient;

    @RabbitListener(queues = RabbitMqConfig.BROADCAST_QUEUE)
    public void consume(BroadcastMessageEvent event) {
        log.info("Received BroadcastMessageEvent, adminId={}, targetCount={}",
                event.getInitiatedByAdminId(),
                event.getTargetUserIds() != null ? event.getTargetUserIds().size() : "ALL");

        if (event.getTargetUserIds() == null || event.getTargetUserIds().isEmpty()) {
            log.warn("Target user IDs are empty for broadcast event. Need user service to provide all IDs.");
            // In a real implementation, you'd fetch all user IDs from user-service
            // For now, this is a placeholder. The orchestrator should provide IDs.
            return;
        }

        var result = telegramApiClient.broadcast(event.getTargetUserIds(), event.getMessage());
        log.info("Broadcast complete: {}/{} successful", result.successful(), result.total());
    }
}
