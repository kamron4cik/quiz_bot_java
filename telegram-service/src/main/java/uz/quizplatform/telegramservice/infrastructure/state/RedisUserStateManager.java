package uz.quizplatform.telegramservice.infrastructure.state;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUserStateManager implements UserStateManager {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PENDING_QUIZ = "telegram:state:pending_quiz:";
    private static final String KEY_WIZARD_STEP = "telegram:state:wizard:step:";
    private static final String KEY_PROFILE_DATA = "telegram:state:wizard:data:";
    private static final long EXPIRY_HOURS = 2;

    @Override
    public void setPendingNewQuiz(Long userId, boolean pending) {
        if (pending) {
            redisTemplate.opsForValue().set(KEY_PENDING_QUIZ + userId, "true", EXPIRY_HOURS, TimeUnit.HOURS);
        } else {
            redisTemplate.delete(KEY_PENDING_QUIZ + userId);
        }
    }

    @Override
    public boolean isPendingNewQuiz(Long userId) {
        String val = redisTemplate.opsForValue().get(KEY_PENDING_QUIZ + userId);
        return "true".equals(val);
    }

    @Override
    public void setWizardStep(Long userId, String step) {
        redisTemplate.opsForValue().set(KEY_WIZARD_STEP + userId, step, EXPIRY_HOURS, TimeUnit.HOURS);
    }

    @Override
    public String getWizardStep(Long userId) {
        return redisTemplate.opsForValue().get(KEY_WIZARD_STEP + userId);
    }

    @Override
    public void clearWizard(Long userId) {
        redisTemplate.delete(KEY_WIZARD_STEP + userId);
        clearProfileData(userId);
    }

    @Override
    public void setProfileData(Long userId, String key, String value) {
        redisTemplate.opsForHash().put(KEY_PROFILE_DATA + userId, key, value);
        redisTemplate.expire(KEY_PROFILE_DATA + userId, EXPIRY_HOURS, TimeUnit.HOURS);
    }

    @Override
    public String getProfileData(Long userId, String key) {
        Object val = redisTemplate.opsForHash().get(KEY_PROFILE_DATA + userId, key);
        return val != null ? val.toString() : null;
    }

    @Override
    public void clearProfileData(Long userId) {
        redisTemplate.delete(KEY_PROFILE_DATA + userId);
    }
}
