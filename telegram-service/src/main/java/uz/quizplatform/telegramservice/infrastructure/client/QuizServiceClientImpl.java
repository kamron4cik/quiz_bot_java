package uz.quizplatform.telegramservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Primary
public class QuizServiceClientImpl implements QuizServiceClient {

    private final RestClient quizServiceClientInstance;
    private final RestClient questionServiceClientInstance;
    private final UserServiceClient userServiceClient;

    public QuizServiceClientImpl(
            @org.springframework.beans.factory.annotation.Qualifier("quizServiceClientInstance") RestClient quizServiceClientInstance,
            @org.springframework.beans.factory.annotation.Qualifier("questionServiceClientInstance") RestClient questionServiceClientInstance,
            UserServiceClient userServiceClient) {
        this.quizServiceClientInstance = quizServiceClientInstance;
        this.questionServiceClientInstance = questionServiceClientInstance;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public Session getPausedSession(Long userId) {
        log.debug("Checking paused session for user: {}", userId);
        try {
            return quizServiceClientInstance.get()
                    .uri("/api/v1/quiz/session/paused?userId={userId}", userId)
                    .retrieve()
                    .body(Session.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Failed to query paused session: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<CategoryDto> getCategoriesForUser(Long userId) {
        log.debug("Fetching categories for user university: {}", userId);
        try {
            var userOpt = userServiceClient.getUser(userId);
            if (userOpt.isEmpty() || userOpt.get().getUniversityId() == null) {
                return List.of();
            }
            UUID universityId = userOpt.get().getUniversityId();
            return questionServiceClientInstance.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/categories")
                            .queryParam("universityId", universityId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CategoryDto>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch user categories: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Stats getUserStats(Long userId) {
        log.debug("Fetching quiz stats for user: {}", userId);
        try {
            return quizServiceClientInstance.get()
                    .uri("/api/v1/stats/{userId}", userId)
                    .retrieve()
                    .body(Stats.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user stats: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getUserRank(Long userId, UserServiceClient.User user) {
        log.debug("Fetching rank for user: {}", userId);
        try {
            Long globalRank = quizServiceClientInstance.get()
                    .uri("/api/v1/leaderboard/rank/{userId}", userId)
                    .retrieve()
                    .body(Long.class);

            if (user != null && user.getUniversityId() != null) {
                Long uniRank = quizServiceClientInstance.get()
                        .uri("/api/v1/leaderboard/rank/{userId}/university/{universityId}",
                                userId, user.getUniversityId())
                        .retrieve()
                        .body(Long.class);
                return String.format(
                        "🏆 <b>Sizning reytingingiz:</b>\n\n" +
                        "🌐 Global reytingda: <b>#%d</b>\n" +
                        "🏫 Universitetingizda: <b>#%d</b>",
                        globalRank, uniRank
                );
            }
            return String.format("🏆 <b>Sizning global reytingingiz:</b> #%d", globalRank);
        } catch (Exception e) {
            log.error("Failed to fetch user rank: {}", e.getMessage());
            return "⚠️ Reytingni yuklashda xatolik yuz berdi.";
        }
    }

    @Override
    public void resumeSession(Long userId) {
        log.info("Requesting session resumption for user {}", userId);
        try {
            quizServiceClientInstance.post()
                    .uri("/api/v1/quiz/resume?userId={userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to resume quiz session: {}", e.getMessage());
        }
    }

    @Override
    public void pauseSession(Long userId) {
        log.info("Requesting session pause for user {}", userId);
        try {
            quizServiceClientInstance.post()
                    .uri("/api/v1/quiz/pause?userId={userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to pause quiz session: {}", e.getMessage());
        }
    }

    @Override
    public void startSession(Long userId, UUID categoryId) {
        log.info("Requesting new session start for user {}, category {}", userId, categoryId);
        try {
            StartQuizRequest payload = new StartQuizRequest(userId, categoryId, "RANDOM", 10, 30, 0);
            quizServiceClientInstance.post()
                    .uri("/api/v1/quiz/start")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to start new quiz session: {}", e.getMessage());
        }
    }

    @Override
    public SubmitPollAnswerResponse submitPollAnswer(String pollId, Long userId, int optionId) {
        log.info("Submitting poll answer: pollId={}, userId={}, optionId={}", pollId, userId, optionId);
        try {
            return quizServiceClientInstance.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/quiz/answer")
                            .queryParam("pollId", pollId)
                            .queryParam("userId", userId)
                            .queryParam("optionId", optionId)
                            .build())
                    .retrieve()
                    .body(SubmitPollAnswerResponse.class);
        } catch (Exception e) {
            log.error("Failed to submit poll answer: {}", e.getMessage());
            return null;
        }
    }

    private record StartQuizRequest(
            Long userId,
            UUID categoryId,
            String mode,
            int questionCount,
            int timePerQuestionSeconds,
            int questionOffset
    ) {}
}
