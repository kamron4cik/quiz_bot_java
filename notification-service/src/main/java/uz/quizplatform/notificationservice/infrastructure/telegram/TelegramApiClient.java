package uz.quizplatform.notificationservice.infrastructure.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * Low-level Telegram API client for the notification service.
 *
 * Replaces V1's TelegramService class with:
 * - Proper retry logic for 429 Too Many Requests
 * - Rate-limited broadcast (30 messages/second)
 * - Structured logging for all failures
 *
 * This service is the ONLY place in the entire platform that calls the Telegram Bot API directly.
 * All other services request message delivery via this service (through notification-service endpoints).
 */
@Slf4j
@Service
public class TelegramApiClient {

    private static final int MAX_RETRIES = 3;
    private static final int BROADCAST_BATCH_SIZE = 30;
    private static final long BROADCAST_BATCH_DELAY_MS = 1100; // Slightly over 1s for safety

    private final OkHttpTelegramClient client;

    public TelegramApiClient(@Value("${telegram.bot.token}") String botToken) {
        this.client = new OkHttpTelegramClient(botToken);
    }

    public Message sendMessage(Long chatId, String text, String parseMode) {
        return executeWithRetry(() ->
                client.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .parseMode(parseMode != null ? parseMode : "HTML")
                        .build()),
                "sendMessage", chatId);
    }

    public Message sendMessageWithMarkup(Long chatId, String text, SendMessage request) {
        return executeWithRetry(() -> client.execute(request), "sendMessageWithMarkup", chatId);
    }

    public Message sendPoll(Long chatId, String question, List<String> options, boolean isAnonymous) {
        var pollRequest = SendPoll.builder()
                .chatId(chatId)
                .question(question)
                .options(options)
                .isAnonymous(isAnonymous)
                .type("quiz")
                .build();
        return executeWithRetry(() -> client.execute(pollRequest), "sendPoll", chatId);
    }

    public boolean deleteMessage(Long chatId, Integer messageId) {
        try {
            client.execute(DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build());
            return true;
        } catch (TelegramApiException e) {
            // Downgraded: deletion failures are expected for old/already-deleted messages
            log.debug("Failed to delete message {} in chat {}: {}", messageId, chatId, e.getMessage());
            return false;
        }
    }

    /**
     * Broadcasts a message to a list of users.
     * Respects Telegram's rate limit: ~30 messages/second.
     * Reports progress after each batch.
     *
     * @return BroadcastResult with success/fail counts
     */
    public BroadcastResult broadcast(List<Long> userIds, String text) {
        log.info("Starting broadcast to {} users", userIds.size());
        int successful = 0;
        int failed = 0;

        for (int i = 0; i < userIds.size(); i += BROADCAST_BATCH_SIZE) {
            int end = Math.min(i + BROADCAST_BATCH_SIZE, userIds.size());
            var batch = userIds.subList(i, end);

            for (Long userId : batch) {
                try {
                    sendMessage(userId, text, "HTML");
                    successful++;
                } catch (Exception e) {
                    failed++;
                    log.warn("Broadcast failed for user {}: {}", userId, e.getMessage());
                }
            }

            // Rate limit: wait between batches
            if (end < userIds.size()) {
                try {
                    Thread.sleep(BROADCAST_BATCH_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Broadcast interrupted at {}/{}", end, userIds.size());
                    break;
                }
            }

            log.info("Broadcast progress: {}/{} (✅ {} ❌ {})", end, userIds.size(), successful, failed);
        }

        log.info("Broadcast completed: total={}, successful={}, failed={}", userIds.size(), successful, failed);
        return new BroadcastResult(successful, failed, userIds.size());
    }

    /**
     * Retry wrapper for Telegram API calls.
     * Reads retry_after from Telegram 429 responses and waits accordingly.
     */
    private <T> T executeWithRetry(TelegramApiCallable<T> callable, String method, Long chatId) {
        TelegramApiException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return callable.call();
            } catch (TelegramApiException e) {
                lastException = e;
                String errorMessage = e.getMessage();

                if (errorMessage != null && errorMessage.contains("429")) {
                    // Parse retry_after from error message
                    long retryAfterMs = extractRetryAfter(errorMessage) * 1000L + 500;
                    log.warn("Telegram 429 on {} for chat {}, retry {}/{} after {}ms",
                            method, chatId, attempt, MAX_RETRIES, retryAfterMs);
                    try {
                        Thread.sleep(retryAfterMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else if (attempt < MAX_RETRIES) {
                    log.warn("Telegram error on {} for chat {}, retry {}/{}: {}",
                            method, chatId, attempt, MAX_RETRIES, errorMessage);
                    try {
                        Thread.sleep(1000L * attempt); // exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    break;
                }
            }
        }

        log.error("Telegram {} failed for chat {} after {} attempts", method, chatId, MAX_RETRIES,
                lastException);
        return null; // non-fatal: notification failures should not crash the system
    }

    private long extractRetryAfter(String errorMessage) {
        try {
            var parts = errorMessage.split("retry after ");
            if (parts.length > 1) {
                return Long.parseLong(parts[1].trim().replaceAll("[^0-9]", ""));
            }
        } catch (NumberFormatException ignored) { }
        return 5; // default 5 seconds if we can't parse
    }

    @FunctionalInterface
    private interface TelegramApiCallable<T> {
        T call() throws TelegramApiException;
    }

    public record BroadcastResult(int successful, int failed, int total) {}
}
