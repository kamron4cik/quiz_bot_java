package uz.quizplatform.userservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.PaymentStatusChangedEvent;
import uz.quizplatform.userservice.domain.entity.PaymentRequest;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Must match the exchanges and routing keys in notification-service's RabbitMqConfig
    private static final String PAYMENT_EVENTS_EXCHANGE = "payment.events";
    private static final String PAYMENT_STATUS_KEY = "payment.status.changed";

    public void publishPaymentStatusChanged(PaymentRequest payment) {
        var event = new PaymentStatusChangedEvent(
                UUID.randomUUID().toString(),
                payment.getId().toString(),
                payment.getUserId(),
                payment.getStatus().name(),
                payment.getReviewedBy().toString()
        );

        rabbitTemplate.convertAndSend(PAYMENT_EVENTS_EXCHANGE, PAYMENT_STATUS_KEY, event);
        log.info("Published PaymentStatusChangedEvent for payment {}, status {}", payment.getId(), payment.getStatus());
    }
}
