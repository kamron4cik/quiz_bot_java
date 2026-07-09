package uz.quizplatform.quizservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import uz.quizplatform.quizservice.domain.entity.QuizSession;

import java.util.UUID;

/**
 * Service for maintaining high-performance leaderboards in Redis Sorted Sets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LEADERBOARD_GLOBAL = "leaderboard:global:all_time";
    private static final String LEADERBOARD_UNIVERSITY_PREFIX = "leaderboard:university:";

    public void updateLeaderboard(QuizSession session) {
        if (!session.isTerminal() || session.getUserId() == null) {
            return;
        }

        String userIdStr = session.getUserId().toString();
        // Use totalCorrect as score for now. To break ties, we can add a fraction based on time.
        // For simplicity, we just use score percentage or total correct.
        double score = session.getScore(); 

        log.debug("Updating leaderboard for user {} with score {}", session.getUserId(), score);

        try {
            // Update Global Leaderboard
            updateSortedSetIfHigher(LEADERBOARD_GLOBAL, userIdStr, score);

            // Update University Leaderboard if available
            if (session.getUniversityId() != null) {
                String uniKey = LEADERBOARD_UNIVERSITY_PREFIX + session.getUniversityId().toString() + ":all_time";
                updateSortedSetIfHigher(uniKey, userIdStr, score);
            }
        } catch (Exception e) {
            log.error("Failed to update Redis leaderboard for user {}: {}", session.getUserId(), e.getMessage());
        }
    }

    private void updateSortedSetIfHigher(String key, String member, double newScore) {
        Double existingScore = redisTemplate.opsForZSet().score(key, member);
        if (existingScore == null || newScore > existingScore) {
            redisTemplate.opsForZSet().add(key, member, newScore);
        }
    }
}
