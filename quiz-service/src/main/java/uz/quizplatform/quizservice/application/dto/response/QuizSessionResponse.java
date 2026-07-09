package uz.quizplatform.quizservice.application.dto.response;

import lombok.Builder;
import lombok.Data;
import uz.quizplatform.quizservice.domain.entity.QuizSession;

@Data
@Builder
public class QuizSessionResponse {
    private String sessionId;
    private String categoryName;
    
    public static QuizSessionResponse from(QuizSession session, String categoryName) {
        return QuizSessionResponse.builder()
                .sessionId(session.getId().toString())
                .categoryName(categoryName)
                .build();
    }
}
