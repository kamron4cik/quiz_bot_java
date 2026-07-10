package uz.quizplatform.notificationservice.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import uz.quizplatform.common.domain.event.SendQuizQuestionEvent;
import uz.quizplatform.notificationservice.infrastructure.config.RabbitMqConfig;
import uz.quizplatform.notificationservice.infrastructure.telegram.TelegramApiClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendQuestionEventConsumer {

    private final TelegramApiClient telegramApiClient;
    private final RestClient quizServiceClientInstance;

    @RabbitListener(queues = RabbitMqConfig.SEND_QUESTION_QUEUE)
    public void consume(SendQuizQuestionEvent event) {
        log.info("Received SendQuizQuestionEvent for user {}, question {}", event.getUserId(), event.getQuestionId());
        
        Message message = telegramApiClient.sendPoll(
                event.getUserId(),
                event.getQuestionText(),
                event.getOptions(),
                false // not anonymous, so we can track user's answers via PollAnswer
        );
        
        if (message != null && message.getPoll() != null) {
            String pollId = message.getPoll().getId();
            Long messageId = (long) message.getMessageId();
            log.info("Telegram poll sent successfully. pollId={}, messageId={}", pollId, messageId);

            // Notify quiz-service of the generated poll details
            try {
                UpdatePollRequest request = new UpdatePollRequest(
                        pollId,
                        event.getOptions(),
                        event.getCorrectOptionIndex(),
                        messageId
                );
                
                quizServiceClientInstance.put()
                        .uri("/api/v1/quiz/sessions/{sessionId}/poll", event.getSessionId())
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                
                log.info("Successfully updated quiz session {} with poll details", event.getSessionId());
            } catch (Exception e) {
                log.error("Failed to update quiz-service for session {}: {}", event.getSessionId(), e.getMessage());
            }
        } else {
            log.error("Failed to send Telegram poll for session {}, question {}", event.getSessionId(), event.getQuestionId());
        }
    }

    public record UpdatePollRequest(
            String pollId,
            List<String> shuffledOptions,
            int correctOptionIndex,
            Long messageId
    ) {}
}
