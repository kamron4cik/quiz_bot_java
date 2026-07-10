package uz.quizplatform.telegramservice.infrastructure.sender;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface TelegramMessageSender {
    void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard);
    java.io.InputStream downloadFile(String fileId) throws Exception;
}
