package uz.quizplatform.quizservice.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import uz.quizplatform.quizservice.domain.entity.QuizSession;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisQuizSessionCache implements QuizSessionCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "quiz:session:";
    private static final long EXPIRY_HOURS = 24;

    @Override
    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    @Override
    public void put(Long userId, QuizSession session) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, session, EXPIRY_HOURS, TimeUnit.HOURS);
    }
}
