package uz.quizplatform.adminservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.BroadcastMessageEvent;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String NOTIFICATION_EXCHANGE = "notification";
    private static final String BROADCAST_KEY = "notification.broadcast";

    public void publishBroadcast(String message, String parseMode, List<Long> targetUserIds, Long adminId) {
        var event = new BroadcastMessageEvent(
                UUID.randomUUID().toString(),
                message,
                parseMode,
                targetUserIds,
                adminId
        );

        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, BROADCAST_KEY, event);
        log.info("Published BroadcastMessageEvent by admin {}", adminId);
    }
}
