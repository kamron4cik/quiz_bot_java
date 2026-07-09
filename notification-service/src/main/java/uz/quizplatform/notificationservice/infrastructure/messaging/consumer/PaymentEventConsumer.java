package uz.quizplatform.notificationservice.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.PaymentStatusChangedEvent;
import uz.quizplatform.notificationservice.infrastructure.config.RabbitMqConfig;
import uz.quizplatform.notificationservice.infrastructure.telegram.TelegramApiClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final TelegramApiClient telegramApiClient;

    @RabbitListener(queues = RabbitMqConfig.PAYMENT_STATUS_QUEUE)
    public void consume(PaymentStatusChangedEvent event) {
        log.info("Received PaymentStatusChangedEvent: paymentId={}, status={}", event.getPaymentId(), event.getNewStatus());

        String message;
        if ("APPROVED".equals(event.getNewStatus())) {
            message = "✅ <b>To'lovingiz tasdiqlandi!</b>\n\nSiz endi testlardan to'liq foydalanishingiz mumkin. Omad!";
        } else if ("REJECTED".equals(event.getNewStatus())) {
            message = "❌ <b>To'lovingiz rad etildi!</b>\n\nIltimos, qaytadan urinib ko'ring yoki admin bilan bog'laning.";
        } else {
            return;
        }

        telegramApiClient.sendMessage(event.getUserId(), message, "HTML");
    }
}
