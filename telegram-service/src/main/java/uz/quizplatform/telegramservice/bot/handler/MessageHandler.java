package uz.quizplatform.telegramservice.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.quizplatform.telegramservice.bot.keyboard.MainMenuKeyboard;
import uz.quizplatform.telegramservice.infrastructure.client.UserServiceClient;
import uz.quizplatform.telegramservice.infrastructure.client.QuizServiceClient;
import uz.quizplatform.telegramservice.infrastructure.sender.TelegramMessageSender;
import uz.quizplatform.telegramservice.infrastructure.state.UserStateManager;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final UserServiceClient userServiceClient;
    private final QuizServiceClient quizServiceClient;
    private final TelegramMessageSender sender;
    private final UserStateManager stateManager;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final uz.quizplatform.telegramservice.bot.keyboard.ProfileWizardKeyboard wizardKeyboard;

    public void handle(Update update) {
        var message = update.getMessage();
        var userId = message.getFrom().getId();
        var chatId = message.getChatId();

        // 1. Handle Photo Uploads (Payment Receipts)
        if (message.hasPhoto() && !message.getPhoto().isEmpty()) {
            var photos = message.getPhoto();
            // Get the largest size photo for better quality resolution
            var largestPhoto = photos.get(photos.size() - 1);
            String fileId = largestPhoto.getFileId();

            log.info("Payment receipt upload detected from user {}. FileId={}", userId, fileId);
            userServiceClient.submitPayment(userId, fileId);

            sender.sendMessage(chatId,
                    "✅ <b>To'lov cheki qabul qilindi!</b>\n\n" +
                    "Administrator chekni tekshirib, 10-15 daqiqa ichida testga to'liq ruxsat beradi. Kuting.",
                    null);
            return;
        }

        // 1.2 Handle Document Uploads (Admin Question Import files)
        if (message.hasDocument()) {
            var userOpt = userServiceClient.getUser(userId);
            if (userOpt.isPresent() && userOpt.get().isAdmin()) {
                var doc = message.getDocument();
                stateManager.setProfileData(userId, "pendingImportFileId", doc.getFileId());
                stateManager.setProfileData(userId, "pendingImportFileName", doc.getFileName());
                
                var categories = quizServiceClient.getCategoriesForUser(userId);
                sender.sendMessage(chatId, "📥 <b>Fayl qabul qilindi!</b>\n\nSavollarni qaysi kategoriyaga yuklamoqchisiz? Tanlang:",
                        wizardKeyboard.categorySelection(categories));
            } else {
                sender.sendMessage(chatId, "❌ Hujjat yuklash faqat administratorlar uchun ruxsat etilgan.", null);
            }
            return;
        }

        // 2. Handle Text Inputs (Academic Onboarding Wizard)
        if (message.hasText()) {
            String text = message.getText().trim();
            String step = stateManager.getWizardStep(userId);

            if (step == null) {
                // If not in onboarding and they typed something, send standard info
                var userOpt = userServiceClient.getUser(userId);
                if (userOpt.isPresent()) {
                    sender.sendMessage(chatId, "👋 Tanlovni amalga oshirish uchun menyu tugmalaridan foydalaning.",
                            mainMenuKeyboard.build(userOpt.get()));
                }
                return;
            }

            log.debug("User {} in step {} typed: {}", userId, step, text);

            switch (step) {
                case "MAJOR_INPUT" -> {
                    stateManager.setProfileData(userId, "major", text);
                    stateManager.setWizardStep(userId, "GRADE_INPUT");
                    sender.sendMessage(chatId, "🏫 <b>Kursingizni kiriting (faqat raqam 1 dan 5 gacha):</b>", null);
                }
                
                case "GRADE_INPUT" -> {
                    try {
                        int grade = Integer.parseInt(text);
                        if (grade < 1 || grade > 5) {
                            throw new IllegalArgumentException();
                        }
                        stateManager.setProfileData(userId, "grade", String.valueOf(grade));
                        stateManager.setWizardStep(userId, "STUDY_METHOD");
                        sender.sendMessage(chatId,
                                "📚 <b>Ta'lim shaklini tanlang/kiriting:</b>\n" +
                                "(kunduzgi, kechki, sirtqi, masofaviy)", null);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(chatId, "⚠️ Iltimos, faqat 1 dan 5 gacha bo'lgan raqam kiriting:", null);
                    }
                }
                
                case "STUDY_METHOD" -> {
                    String method = text.toLowerCase();
                    if (!List.of("kunduzgi", "kechki", "sirtqi", "masofaviy").contains(method)) {
                        sender.sendMessage(chatId, "⚠️ Noto'g'ri ta'lim shakli. Iltimos quyidagilardan birini yozing:\n" +
                                "(kunduzgi, kechki, sirtqi, masofaviy)", null);
                        return;
                    }
                    stateManager.setProfileData(userId, "studyMethod", method);
                    stateManager.setWizardStep(userId, "TEST_TYPE");
                    sender.sendMessage(chatId,
                            "📝 <b>Test turini kiriting:</b>\n" +
                            "(oraliq, yakuniy)", null);
                }
                
                case "TEST_TYPE" -> {
                    String testType = text.toLowerCase();
                    if (!List.of("oraliq", "yakuniy").contains(testType)) {
                        sender.sendMessage(chatId, "⚠️ Noto'g'ri test turi. Iltimos quyidagilardan birini yozing:\n" +
                                "(oraliq, yakuniy)", null);
                        return;
                    }
                    stateManager.setProfileData(userId, "testType", testType);

                    // All details collected -> update profile in user-service
                    try {
                        UUID universityId = UUID.fromString(stateManager.getProfileData(userId, "universityId"));
                        String major = stateManager.getProfileData(userId, "major");
                        int grade = Integer.parseInt(stateManager.getProfileData(userId, "grade"));
                        String studyMethod = stateManager.getProfileData(userId, "studyMethod");

                        UserServiceClient.UpdateProfileRequest request = new UserServiceClient.UpdateProfileRequest(
                                universityId, major, grade, studyMethod, testType
                        );

                        userServiceClient.updateProfile(userId, request);
                        stateManager.clearWizard(userId);

                        var userOpt = userServiceClient.getUser(userId);
                        if (userOpt.isPresent()) {
                            sender.sendMessage(chatId,
                                    "🎉 <b>Profilingiz muvaffaqiyatli to'ldirildi!</b>\n\nMenyudan kerakli bo'limni tanlashingiz mumkin.",
                                    mainMenuKeyboard.build(userOpt.get()));
                        } else {
                            sender.sendMessage(chatId, "🎉 <b>Profilingiz muvaffaqiyatli to'ldirildi!</b>", null);
                        }
                    } catch (Exception e) {
                        log.error("Failed to complete user profile registration", e);
                        sender.sendMessage(chatId, "⚠️ Ro'yxatdan o'tishda xatolik yuz berdi. Iltimos /start yozib qaytadan urinib ko'ring.", null);
                        stateManager.clearWizard(userId);
                    }
                }
            }
        }
    }
}
