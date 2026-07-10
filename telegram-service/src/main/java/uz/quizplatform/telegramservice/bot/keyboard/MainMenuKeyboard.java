package uz.quizplatform.telegramservice.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import uz.quizplatform.telegramservice.infrastructure.client.UserServiceClient.User;

import java.util.ArrayList;
import java.util.List;

@Component
public class MainMenuKeyboard {

    public InlineKeyboardMarkup build(User user) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("📚 Testni boshlash").callbackData("menu:start_quiz").build(),
                InlineKeyboardButton.builder().text("📊 Statistika").callbackData("menu:stats").build()
        ));
        
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🏆 Reyting").callbackData("menu:leaderboard").build(),
                InlineKeyboardButton.builder().text("👤 Profilni qayta tanlash").callbackData("menu:reset_profile").build()
        ));

        if (user.isAdmin()) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder().text("⚙️ Admin panel").callbackData("menu:admin").build()
            ));
        }

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public InlineKeyboardMarkup buildBuyAccess() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("💳 To'lov qilish").callbackData("menu:pay").build()
        ));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public InlineKeyboardMarkup buildPausedSessionOptions() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("▶️ Davom ettirish").callbackData("quiz:resume").build(),
                InlineKeyboardButton.builder().text("🔄 Yangidan boshlash").callbackData("quiz:discard_and_new").build()
        ));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public InlineKeyboardMarkup buildLeaderboardMenu() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🌐 Global reyting").callbackData("leaderboard:global").build(),
                InlineKeyboardButton.builder().text("🏫 Universitet reytingi").callbackData("leaderboard:university").build()
        ));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public InlineKeyboardMarkup buildAdminPanel() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("📈 Tizim statistikasi").callbackData("admin:stats").build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("✉️ Broadcast xabar yuborish").callbackData("admin:broadcast").build()
        ));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
