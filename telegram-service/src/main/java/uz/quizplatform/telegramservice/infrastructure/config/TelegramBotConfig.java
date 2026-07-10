package uz.quizplatform.telegramservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import uz.quizplatform.telegramservice.bot.QuizBotUpdateConsumer;

/**
 * Registers the Telegram bot with the long-polling starter and exposes
 * the {@link TelegramClient} bean needed by {@link uz.quizplatform.telegramservice.infrastructure.sender.TelegramMessageSenderImpl}.
 *
 * The telegrambots-springboot-longpolling-starter auto-configures
 * {@link TelegramBotsLongPollingApplication} and starts polling automatically
 * when bots are registered in this configuration.
 */
@Slf4j
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bots[0].token}")
    private String botToken;

    /**
     * The OkHttp-based TelegramClient used by all senders.
     * Tied to the bot token so that API calls are authenticated.
     */
    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(botToken);
    }

    /**
     * Registers the bot with the long-polling application so that updates
     * start flowing into {@link QuizBotUpdateConsumer}.
     */
    @Bean
    public LongPollingUpdateConsumer botRegistration(
            TelegramBotsLongPollingApplication botsApplication,
            QuizBotUpdateConsumer updateConsumer) {
        try {
            botsApplication.registerBot(botToken, updateConsumer);
            log.info("Telegram bot registered successfully with long-polling");
        } catch (Exception e) {
            log.error("Failed to register Telegram bot: {}", e.getMessage(), e);
            throw new IllegalStateException("Cannot start without a registered Telegram bot", e);
        }
        return updateConsumer;
    }
}
