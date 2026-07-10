package uz.quizplatform.telegramservice.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.quizplatform.telegramservice.bot.keyboard.MainMenuKeyboard;
import uz.quizplatform.telegramservice.bot.keyboard.ProfileWizardKeyboard;
import uz.quizplatform.telegramservice.infrastructure.client.QuizServiceClient;
import uz.quizplatform.telegramservice.infrastructure.client.UserServiceClient;
import uz.quizplatform.telegramservice.infrastructure.sender.TelegramMessageSender;
import uz.quizplatform.telegramservice.infrastructure.state.UserStateManager;
import org.springframework.web.client.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import java.io.InputStream;

import java.util.UUID;

@Slf4j
@Component
public class CallbackQueryHandler {

    private final UserServiceClient userServiceClient;
    private final QuizServiceClient quizServiceClient;
    private final TelegramMessageSender sender;
    private final UserStateManager stateManager;
    private final ProfileWizardKeyboard wizardKeyboard;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final RestClient importServiceClientInstance;

    public CallbackQueryHandler(
            UserServiceClient userServiceClient,
            QuizServiceClient quizServiceClient,
            TelegramMessageSender sender,
            UserStateManager stateManager,
            ProfileWizardKeyboard wizardKeyboard,
            MainMenuKeyboard mainMenuKeyboard,
            @org.springframework.beans.factory.annotation.Qualifier("importServiceClientInstance") RestClient importServiceClientInstance) {
        this.userServiceClient = userServiceClient;
        this.quizServiceClient = quizServiceClient;
        this.sender = sender;
        this.stateManager = stateManager;
        this.wizardKeyboard = wizardKeyboard;
        this.mainMenuKeyboard = mainMenuKeyboard;
        this.importServiceClientInstance = importServiceClientInstance;
    }

    public void handle(Update update) {
        var query = update.getCallbackQuery();
        var data = query.getData();
        var userId = query.getFrom().getId();
        var chatId = query.getMessage().getChatId();

        log.debug("Callback query received: user={}, data={}", userId, data);

        if (data.startsWith("wizard:university:")) {
            String uniId = data.substring("wizard:university:".length());
            stateManager.setProfileData(userId, "universityId", uniId);
            stateManager.setWizardStep(userId, "MAJOR_INPUT");
            sender.sendMessage(chatId, "✍️ <b>Mutaxassisligingiz (yo'nalish)ni kiriting (masalan: Iqtisodiyot):</b>", null);
            
        } else if (data.startsWith("quiz:category:")) {
            String catId = data.substring("quiz:category:".length());
            String pendingFileId = stateManager.getProfileData(userId, "pendingImportFileId");
            if (pendingFileId != null) {
                handleDocumentImport(userId, chatId, catId, pendingFileId);
            } else {
                quizServiceClient.startSession(userId, UUID.fromString(catId));
                sender.sendMessage(chatId, "🚀 <b>Test boshlandi!</b> Savollar Telegram poll ko'rinishida yuboriladi.", null);
            }

        } else if ("menu:start_quiz".equals(data)) {
            handleQuizStart(userId, chatId);

        } else if ("menu:stats".equals(data)) {
            handleStats(userId, chatId);

        } else if ("menu:leaderboard".equals(data)) {
            sender.sendMessage(chatId, "🏆 <b>Reyting turini tanlang:</b>", mainMenuKeyboard.buildLeaderboardMenu());

        } else if ("menu:reset_profile".equals(data)) {
            userServiceClient.resetProfile(userId);
            stateManager.clearWizard(userId);
            var universities = userServiceClient.getUniversities();
            sender.sendMessage(chatId, "🎓 <b>Profilingiz tozalandi.</b> Qaytadan ro'yxatdan o'tish uchun universitetni tanlang:",
                    wizardKeyboard.universitySelection(universities));

        } else if ("menu:pay".equals(data)) {
            sender.sendMessage(chatId,
                    "💳 <b>To'lov tafsilotlari:</b>\n\n" +
                    "Suma: <b>15 000 UZS</b>\n" +
                    "Karta: <code>8600123456789012</code>\n" +
                    "Ega: <b>Kamron Jumanov</b>\n\n" +
                    "To'lov qilgach, chek rasmini (screenshot) shu yerga yuboring. Administrator to'lovingizni tekshirib tasdiqlaydi.",
                    null);

        } else if ("quiz:resume".equals(data)) {
            quizServiceClient.resumeSession(userId);
            sender.sendMessage(chatId, "▶️ Test davom ettirilmoqda...", null);

        } else if ("quiz:discard_and_new".equals(data)) {
            stateManager.setPendingNewQuiz(userId, false);
            quizServiceClient.pauseSession(userId); // pause existing session
            var categories = quizServiceClient.getCategoriesForUser(userId);
            if (categories.isEmpty()) {
                sender.sendMessage(chatId, "📭 Kategoriyalar topilmadi.", null);
            } else {
                sender.sendMessage(chatId, "📚 <b>Kategoriyani tanlang:</b>", wizardKeyboard.categorySelection(categories));
            }

        } else if (data.startsWith("leaderboard:")) {
            handleLeaderboardSelection(userId, chatId, data.substring("leaderboard:".length()));
        }
    }

    private void handleQuizStart(Long userId, Long chatId) {
        var user = userServiceClient.getUser(userId).orElse(null);
        if (user == null) return;

        if (!user.isProfileComplete()) {
            sender.sendMessage(chatId, "⚠️ Testni boshlash uchun avval profilingizni to'ldiring.", null);
            return;
        }

        if (!user.isHasPaid() && !user.isAdmin()) {
            sender.sendMessage(chatId, "❌ Testdan foydalanish uchun to'lov qiling.", mainMenuKeyboard.buildBuyAccess());
            return;
        }

        var categories = quizServiceClient.getCategoriesForUser(userId);
        if (categories.isEmpty()) {
            sender.sendMessage(chatId, "📭 Mos kategoriyalar topilmadi.", null);
            return;
        }
        sender.sendMessage(chatId, "📚 <b>Kategoriyani tanlang:</b>", wizardKeyboard.categorySelection(categories));
    }

    private void handleStats(Long userId, Long chatId) {
        var stats = quizServiceClient.getUserStats(userId);
        if (stats == null) {
            sender.sendMessage(chatId, "📊 Sizda hali statistika mavjud emas.", null);
            return;
        }
        String text = String.format(
            "📊 <b>Sizning statistikangiz</b>\n\n" +
            "✅ Yechilgan testlar: <b>%d</b>\n" +
            "📝 Yechilgan savollar: <b>%d</b>\n" +
            "🎯 O'rtacha ball: <b>%d%%</b>\n" +
            "🏆 Eng yuqori ball: <b>%d%%</b>\n" +
            "⏱ Umumiy vaqt: <b>%s</b>",
            stats.testsCompleted(),
            stats.questionsSolved(),
            stats.averageScore(),
            stats.bestScore(),
            formatDuration(stats.totalStudyTimeSec())
        );
        sender.sendMessage(chatId, text, null);
    }

    private void handleLeaderboardSelection(Long userId, Long chatId, String type) {
        // Since we are decoupling, global rank can be read directly from client rank format logic
        var user = userServiceClient.getUser(userId).orElse(null);
        String rankText = quizServiceClient.getUserRank(userId, user);
        sender.sendMessage(chatId, rankText, null);
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) return seconds + " soniya";
        if (seconds < 3600) return (seconds / 60) + " daqiqa";
        return (seconds / 3600) + " soat " + ((seconds % 3600) / 60) + " daqiqa";
    }

    private void handleDocumentImport(Long userId, Long chatId, String catId, String fileId) {
        String fileName = stateManager.getProfileData(userId, "pendingImportFileName");
        if (fileName == null) fileName = "questions.docx";

        // Clear state
        stateManager.setProfileData(userId, "pendingImportFileId", null);
        stateManager.setProfileData(userId, "pendingImportFileName", null);

        sender.sendMessage(chatId, "⏳ <b>Fayl yuklab olinmoqda va tahlil qilinmoqda...</b> Iltimos kuting.", null);

        try {
            // 1. Download file from Telegram
            InputStream fileStream = sender.downloadFile(fileId);
            byte[] fileBytes = fileStream.readAllBytes();

            // 2. Upload file to MinIO via import-service
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return "file"; // Dummy name
                }
            };
            body.add("file", fileResource);

            UploadResponse uploadResponse = importServiceClientInstance.post()
                    .uri("/api/v1/imports/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(UploadResponse.class);

            if (uploadResponse == null || uploadResponse.filePath() == null) {
                throw new RuntimeException("Failed to upload document to storage service");
            }

            // 3. Trigger import job
            var user = userServiceClient.getUser(userId).orElseThrow();
            CreateImportJobRequest importReq = new CreateImportJobRequest(
                    userId,
                    user.getUniversityId(),
                    UUID.fromString(catId),
                    uploadResponse.filePath(),
                    fileName,
                    fileName.endsWith(".txt") ? "TXT" : "DOCX"
            );

            importServiceClientInstance.post()
                    .uri("/api/v1/imports/jobs")
                    .body(importReq)
                    .retrieve()
                    .toBodilessEntity();

            sender.sendMessage(chatId, "✅ <b>Fayl muvaffaqiyatli import qilindi!</b>\n\nSavollar fon rejimida o'qib chiqilib, kategoriyaga qo'shiladi.", null);

        } catch (Exception e) {
            log.error("Failed to parse and import document", e);
            sender.sendMessage(chatId, "❌ <b>Hujjatni import qilishda xatolik yuz berdi:</b>\n" + e.getMessage(), null);
        }
    }

    private record UploadResponse(String filePath, String originalFilename) {}
    private record CreateImportJobRequest(
            Long adminId,
            UUID universityId,
            UUID categoryId,
            String filePath,
            String originalFilename,
            String fileFormat
    ) {}
}
