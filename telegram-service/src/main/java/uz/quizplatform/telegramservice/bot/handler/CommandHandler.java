package uz.quizplatform.telegramservice.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.quizplatform.telegramservice.bot.keyboard.MainMenuKeyboard;
import uz.quizplatform.telegramservice.bot.keyboard.ProfileWizardKeyboard;
import uz.quizplatform.telegramservice.infrastructure.client.UserServiceClient;
import uz.quizplatform.telegramservice.infrastructure.client.QuizServiceClient;
import uz.quizplatform.telegramservice.infrastructure.sender.TelegramMessageSender;
import uz.quizplatform.telegramservice.infrastructure.state.UserStateManager;

/**
 * Handles Telegram slash commands: /start, /quiz, /stats, /top, /rank, /resume, /admin
 *
 * Maps to V1's command.handler.js.
 * NO business logic here — all calls go to backend services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final UserServiceClient userServiceClient;
    private final QuizServiceClient quizServiceClient;
    private final TelegramMessageSender sender;
    private final UserStateManager stateManager;
    private final ProfileWizardKeyboard wizardKeyboard;
    private final MainMenuKeyboard mainMenuKeyboard;

    public void handle(Update update) {
        var message = update.getMessage();
        var userId = message.getFrom().getId();
        var text = message.getText().split(" ")[0]; // strip command args

        // Register/refresh user on every command
        userServiceClient.registerOrUpdate(message.getFrom());

        switch (text) {
            case "/start" -> handleStart(userId, message.getChatId());
            case "/quiz", "/question", "/category" -> handleQuizStart(userId, message.getChatId());
            case "/stats" -> handleStats(userId, message.getChatId());
            case "/top", "/leaderboard" -> handleLeaderboard(userId, message.getChatId());
            case "/rank" -> handleRank(userId, message.getChatId());
            case "/resume" -> handleResume(userId, message.getChatId());
            case "/admin" -> handleAdmin(userId, message.getChatId());
            default -> log.debug("Unknown command {} from user {}", text, userId);
        }
    }

    private void handleStart(Long userId, Long chatId) {
        var user = userServiceClient.getUser(userId).orElse(null);
        if (user == null) return;

        if (!user.isProfileComplete()) {
            // Start the 5-step profile wizard
            var universities = userServiceClient.getUniversities();
            sender.sendMessage(chatId,
                    "🎓 <b>Salom! Quiz Platformaga xush kelibsiz!</b>\n\n" +
                    "Platformadan foydalanish uchun birinchi navbatda universitetingizni tanlang:",
                    wizardKeyboard.universitySelection(universities));
        } else {
            sender.sendMessage(chatId,
                    "👋 <b>Salom, " + user.getFirstName() + "!</b>\n\nQuiz platformaga xush kelibsiz.",
                    mainMenuKeyboard.build(user));
        }
    }

    private void handleQuizStart(Long userId, Long chatId) {
        var user = userServiceClient.getUser(userId).orElse(null);
        if (user == null) return;

        if (!user.isProfileComplete()) {
            sender.sendMessage(chatId,
                    "⚠️ Testni boshlash uchun avval profilingizni to'ldiring.",
                    null);
            return;
        }

        if (!user.isHasPaid() && !user.isAdmin()) {
            sender.sendMessage(chatId,
                    "❌ <b>Testdan foydalanish uchun to'lov qilish kerak.</b>\n\n" +
                    "To'lov qilish uchun quyidagi tugmani bosing:",
                    mainMenuKeyboard.buildBuyAccess());
            return;
        }

        // Check for paused session
        var pausedSession = quizServiceClient.getPausedSession(userId);
        if (pausedSession != null) {
            sender.sendMessage(chatId,
                    "⏸ <b>Sizda to'xtatilgan test mavjud.</b>\n\n" +
                    "📚 Kategoriya: " + pausedSession.categoryName() + "\n" +
                    "📝 Savol: " + (pausedSession.currentQuestionIndex() + 1) + "/" + pausedSession.questionCount() + "\n\n" +
                    "Davom etasizmi yoki yangi test boshlaysizmi?",
                    mainMenuKeyboard.buildPausedSessionOptions());
            stateManager.setPendingNewQuiz(userId, true);
            return;
        }

        // Show categories
        var categories = quizServiceClient.getCategoriesForUser(userId);
        if (categories.isEmpty()) {
            sender.sendMessage(chatId,
                    "📭 Hozircha sizning profilingizga mos kategoriyalar mavjud emas.", null);
            return;
        }

        sender.sendMessage(chatId, "📚 <b>Kategoriyani tanlang:</b>",
                wizardKeyboard.categorySelection(categories));
    }

    private void handleStats(Long userId, Long chatId) {
        var stats = quizServiceClient.getUserStats(userId);
        if (stats == null) {
            sender.sendMessage(chatId, "📊 Sizda hali statistika yo'q. Birinchi testni boshlang!", null);
            return;
        }

        String text = String.format(
            "📊 <b>Sizning statistikangiz</b>\n\n" +
            "✅ Yechilgan testlar: <b>%d</b>\n" +
            "📝 Yechilgan savollar: <b>%d</b>\n" +
            "🎯 O'rtacha ball: <b>%d%%</b>\n" +
            "🏆 Eng yuqori ball: <b>%d%%</b>\n" +
            "⏱ Umumiy o'qish vaqti: <b>%s</b>",
            stats.testsCompleted(),
            stats.questionsSolved(),
            stats.averageScore(),
            stats.bestScore(),
            formatDuration(stats.totalStudyTimeSec())
        );
        sender.sendMessage(chatId, text, null);
    }

    private void handleLeaderboard(Long userId, Long chatId) {
        sender.sendMessage(chatId, "🏆 <b>Reyting turini tanlang:</b>",
                mainMenuKeyboard.buildLeaderboardMenu());
    }

    private void handleRank(Long userId, Long chatId) {
        var user = userServiceClient.getUser(userId).orElse(null);
        var rankText = quizServiceClient.getUserRank(userId, user);
        sender.sendMessage(chatId, rankText, null);
    }

    private void handleResume(Long userId, Long chatId) {
        var paused = quizServiceClient.getPausedSession(userId);
        if (paused == null) {
            sender.sendMessage(chatId, "⚠️ Davom ettirilishi mumkin bo'lgan test topilmadi.", null);
            return;
        }
        quizServiceClient.resumeSession(userId);
        sender.sendMessage(chatId, "▶️ Test davom ettirildi.", null);
    }

    private void handleAdmin(Long userId, Long chatId) {
        var user = userServiceClient.getUser(userId).orElse(null);
        if (user == null || !user.isAdmin()) {
            sender.sendMessage(chatId, "❌ Siz admin emassiz.", null);
            return;
        }
        sender.sendMessage(chatId, "⚙️ <b>Admin panel</b>\n\nBo'limni tanlang:",
                mainMenuKeyboard.buildAdminPanel());
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) return seconds + " soniya";
        if (seconds < 3600) return (seconds / 60) + " daqiqa";
        return (seconds / 3600) + " soat " + ((seconds % 3600) / 60) + " daqiqa";
    }
}
