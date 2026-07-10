package uz.quizplatform.telegramservice.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Production implementation of TelegramMessageSender.
 *
 * Uses the telegrambots v7 TelegramClient (injected by the Spring Boot long-polling starter)
 * to send HTML-formatted messages with optional inline keyboards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageSenderImpl implements TelegramMessageSender {

    private final TelegramClient telegramClient;

    @Override
    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        var message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId {}: {}", chatId, e.getMessage(), e);
        }
    }

    @Override
    public java.io.InputStream downloadFile(String fileId) throws Exception {
        var getFile = org.telegram.telegrambots.meta.api.methods.GetFile.builder()
                .fileId(fileId)
                .build();
        org.telegram.telegrambots.meta.api.objects.File file = telegramClient.execute(getFile);
        return telegramClient.downloadFileAsStream(file);
    }
}
