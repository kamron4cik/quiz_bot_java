package uz.quizplatform.notificationservice.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.SendTextMessageEvent;
import uz.quizplatform.notificationservice.infrastructure.config.RabbitMqConfig;
import uz.quizplatform.notificationservice.infrastructure.telegram.TelegramApiClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class SingleMessageEventConsumer {

    private final TelegramApiClient telegramApiClient;

    @RabbitListener(queues = RabbitMqConfig.SINGLE_MSG_QUEUE)
    public void consume(SendTextMessageEvent event) {
        log.info("Received SendTextMessageEvent for user {}", event.getUserId());
        telegramApiClient.sendMessage(event.getUserId(), event.getMessage(), event.getParseMode());
    }
}
