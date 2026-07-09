package uz.quizplatform.telegramservice.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class MainMenuKeyboard {
    public InlineKeyboardMarkup build(Object user) {
        return null;
    }
    public InlineKeyboardMarkup buildBuyAccess() {
        return null;
    }
    public InlineKeyboardMarkup buildPausedSessionOptions() {
        return null;
    }
    public InlineKeyboardMarkup buildLeaderboardMenu() {
        return null;
    }
    public InlineKeyboardMarkup buildAdminPanel() {
        return null;
    }
}
