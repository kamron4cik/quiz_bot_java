package uz.quizplatform.quizservice.infrastructure.client;

import java.util.List;
import java.util.UUID;

public interface QuestionServiceClient {
    List<UUID> getQuestionIdsByCategory(UUID categoryId);
}
