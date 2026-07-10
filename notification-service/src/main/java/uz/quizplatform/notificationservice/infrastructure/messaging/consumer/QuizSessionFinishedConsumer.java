package uz.quizplatform.notificationservice.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.QuizSessionFinishedEvent;
import uz.quizplatform.notificationservice.infrastructure.config.RabbitMqConfig;
import uz.quizplatform.notificationservice.infrastructure.telegram.TelegramApiClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizSessionFinishedConsumer {

    private final TelegramApiClient telegramApiClient;

    @RabbitListener(queues = RabbitMqConfig.SESSION_FINISHED_QUEUE)
    public void consume(QuizSessionFinishedEvent event) {
        log.info("Received QuizSessionFinishedEvent for user {}, session {}", event.getUserId(), event.getSessionId());

        long minutes = event.getDurationSeconds() / 60;
        long seconds = event.getDurationSeconds() % 60;
        String durationText = minutes > 0 ? minutes + " daqiqa " + seconds + " soniya" : seconds + " soniya";

        String message = String.format(
                "🏁 <b>Test yakunlandi!</b>\n\n" +
                "Kategoriya: <b>%s</b>\n" +
                "Jami savollar: <b>%d</b>\n" +
                "To'g'ri javoblar: <b>%d</b> ✅\n" +
                "Noto'g'ri javoblar: <b>%d</b> ❌\n" +
                "Natija: <b>%d%%</b>\n" +
                "Sarf etilgan vaqt: <b>%s</b>\n\n" +
                "Natijangiz reytingda yangilandi!",
                event.getCategoryName(),
                event.getQuestionCount(),
                event.getTotalCorrect(),
                event.getTotalWrong(),
                event.getScorePercentage(),
                durationText
        );

        telegramApiClient.sendMessage(event.getUserId(), message, "HTML");
    }
}
