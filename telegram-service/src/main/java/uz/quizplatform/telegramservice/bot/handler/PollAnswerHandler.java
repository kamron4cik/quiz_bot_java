package uz.quizplatform.telegramservice.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.quizplatform.telegramservice.infrastructure.client.QuizServiceClient;
import uz.quizplatform.telegramservice.infrastructure.sender.TelegramMessageSender;
import uz.quizplatform.telegramservice.infrastructure.state.UserStateManager;

/**
 * Handles Telegram native poll_answer updates.
 *
 * Maps to V1's callbackHandler.handlePollAnswer → quizService.handlePollAnswer
 *
 * When a user answers a Telegram quiz poll, this handler:
 * 1. Looks up the session by poll_id
 * 2. Calls quiz-service to record the answer
 * 3. Gets the updated session from quiz-service
 * 4. Sends the next question or finishes the quiz
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PollAnswerHandler {

    private final QuizServiceClient quizServiceClient;
    private final TelegramMessageSender sender;
    private final UserStateManager stateManager;

    public void handle(Update update) {
        var pollAnswer = update.getPollAnswer();
        var userId = pollAnswer.getUser().getId();
        var pollId = pollAnswer.getPollId();

        if (pollAnswer.getOptionIds().isEmpty()) {
            // User retracted their vote — ignore
            log.debug("User {} retracted poll answer for poll {}", userId, pollId);
            return;
        }

        int selectedOptionIndex = pollAnswer.getOptionIds().get(0);

        log.debug("Poll answer received: userId={}, pollId={}, optionIndex={}", userId, pollId, selectedOptionIndex);

        try {
            var result = quizServiceClient.submitPollAnswer(pollId, userId, selectedOptionIndex);

            if (result == null) {
                log.warn("No active session found for poll {} user {}", pollId, userId);
                return;
            }

            if (result.isSessionFinished()) {
                // Quiz is complete — results are sent by notification-service via RabbitMQ event
                log.info("Session {} finished for user {} via poll answer", result.sessionId(), userId);
            }
            // Next question delivery is handled by notification-service after processing the answer event

        } catch (Exception e) {
            log.error("Error handling poll answer for user {}, poll {}", userId, pollId, e);
        }
    }
}
