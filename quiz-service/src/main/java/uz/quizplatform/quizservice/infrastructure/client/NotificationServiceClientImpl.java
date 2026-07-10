package uz.quizplatform.quizservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import uz.quizplatform.common.domain.event.SendQuizQuestionEvent;
import uz.quizplatform.common.domain.event.SendTextMessageEvent;
import uz.quizplatform.quizservice.domain.entity.QuizSession;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceClientImpl implements NotificationServiceClient {

    private final RabbitTemplate rabbitTemplate;
    private final QuestionServiceClient questionServiceClient;

    private static final String QUIZ_EXCHANGE = "quiz.events";
    private static final String NOTIFICATION_EXCHANGE = "notification";

    private static final String ROUTING_KEY_QUESTION = "quiz.send.question";
    private static final String ROUTING_KEY_TEXT = "notification.single";

    @Override
    public void sendTimeoutWarning(Long userId, String message) {
        log.info("Sending timeout warning to user {}", userId);
        SendTextMessageEvent event = new SendTextMessageEvent(
                UUID.randomUUID().toString(),
                userId,
                message,
                "HTML"
        );
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_TEXT, event);
    }

    @Override
    public void sendSessionResults(QuizSession session) {
        // No-op because QuizEventPublisher already publishes QuizSessionFinishedEvent,
        // which QuizSessionFinishedConsumer handles directly in notification-service.
        log.info("Skipping explicit notification call for session results, handled viaFinishedEvent.");
    }

    @Override
    public void sendNextQuestion(QuizSession session) {
        UUID questionId = session.getCurrentQuestionId();
        log.info("Preparing to send next question {} for session {}", questionId, session.getId());

        QuestionServiceClient.QuestionDto question = questionServiceClient.getQuestionById(questionId);
        
        // Invariant: shuffle options and adjust correct index
        List<String> originalOptions = List.of(
                question.optionA(),
                question.optionB(),
                question.optionC(),
                question.optionD()
        );
        List<String> shuffledOptions = new ArrayList<>(originalOptions);
        Collections.shuffle(shuffledOptions);
        
        int correctOptionIndex = shuffledOptions.indexOf(originalOptions.get(question.correctAnswer()));

        SendQuizQuestionEvent event = new SendQuizQuestionEvent(
                UUID.randomUUID().toString(),
                session.getUserId(),
                session.getId(),
                questionId,
                question.text(),
                shuffledOptions,
                correctOptionIndex,
                session.getTimePerQuestionSeconds()
        );

        rabbitTemplate.convertAndSend(QUIZ_EXCHANGE, ROUTING_KEY_QUESTION, event);
    }

    @Override
    public void sendInactivityTimeoutNotification(QuizSession session) {
        log.info("Sending inactivity timeout notification to user {}", session.getUserId());
        String message = "⚠️ <b>Test faollik bo'lmagani sababli yakunlandi!</b>\n\nNatijalarni olish uchun keyingi safar tezroq javob bering.";
        SendTextMessageEvent event = new SendTextMessageEvent(
                UUID.randomUUID().toString(),
                session.getUserId(),
                message,
                "HTML"
        );
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_TEXT, event);
    }
}
