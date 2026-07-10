package uz.quizplatform.quizservice.infrastructure.client;

import java.util.List;
import java.util.UUID;

public interface QuestionServiceClient {
    List<UUID> getQuestionIdsByCategory(UUID categoryId);
    
    QuestionDto getQuestionById(UUID questionId);

    record QuestionDto(
            UUID id,
            UUID categoryId,
            String text,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            int correctAnswer,
            String explanation
    ) {
        public List<String> getOptions() {
            return List.of(optionA, optionB, optionC, optionD);
        }
    }
}
