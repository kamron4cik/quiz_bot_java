package uz.quizplatform.telegramservice.infrastructure.client;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StubQuizServiceClient implements QuizServiceClient {
    @Override
    public Session getPausedSession(Long userId) {
        return null;
    }

    @Override
    public List<Object> getCategoriesForUser(Long userId) {
        return List.of();
    }

    @Override
    public Stats getUserStats(Long userId) {
        return null;
    }

    @Override
    public String getUserRank(Long userId, Object user) {
        return "";
    }

    @Override
    public void resumeSession(Long userId) {
    }

    @Override
    public SubmitPollAnswerResponse submitPollAnswer(String pollId, Long userId, int optionId) {
        return null;
    }
}
