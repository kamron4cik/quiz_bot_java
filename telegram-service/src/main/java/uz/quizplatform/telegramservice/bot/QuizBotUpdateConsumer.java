package uz.quizplatform.telegramservice.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.quizplatform.telegramservice.bot.handler.CallbackQueryHandler;
import uz.quizplatform.telegramservice.bot.handler.CommandHandler;
import uz.quizplatform.telegramservice.bot.handler.MessageHandler;
import uz.quizplatform.telegramservice.bot.handler.PollAnswerHandler;

import java.util.List;

/**
 * Main Telegram bot update router.
 *
 * This is the Telegram-layer equivalent of V1's bot/index.js.
 * It receives all Telegram updates and routes them to the appropriate handler.
 *
 * ARCHITECTURAL PRINCIPLE: This class has ZERO business logic.
 * It only routes incoming updates. All logic lives in backend services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizBotUpdateConsumer implements LongPollingUpdateConsumer {

    private final CommandHandler commandHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final PollAnswerHandler pollAnswerHandler;

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(this::processUpdate);
    }

    private void processUpdate(Update update) {
        try {
            if (update.hasMessage()) {
                var message = update.getMessage();
                if (message.hasText() && message.getText().startsWith("/")) {
                    commandHandler.handle(update);
                } else {
                    messageHandler.handle(update);
                }
            } else if (update.hasCallbackQuery()) {
                callbackQueryHandler.handle(update);
            } else if (update.hasPollAnswer()) {
                pollAnswerHandler.handle(update);
            } else {
                log.debug("Unhandled update type: {}", update.getUpdateId());
            }
        } catch (Exception e) {
            log.error("Error processing update {}: {}", update.getUpdateId(), e.getMessage(), e);
        }
    }
}
