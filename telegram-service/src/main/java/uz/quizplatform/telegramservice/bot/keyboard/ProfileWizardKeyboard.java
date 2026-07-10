package uz.quizplatform.telegramservice.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import uz.quizplatform.telegramservice.infrastructure.client.UserServiceClient.UniversityDto;
import uz.quizplatform.telegramservice.infrastructure.client.QuizServiceClient.CategoryDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProfileWizardKeyboard {

    public InlineKeyboardMarkup universitySelection(List<UniversityDto> universities) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (UniversityDto uni : universities) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(uni.name())
                            .callbackData("wizard:university:" + uni.id())
                            .build()
            ));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public InlineKeyboardMarkup categorySelection(List<CategoryDto> categories) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (CategoryDto cat : categories) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(cat.name())
                            .callbackData("quiz:category:" + cat.id())
                            .build()
            ));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
