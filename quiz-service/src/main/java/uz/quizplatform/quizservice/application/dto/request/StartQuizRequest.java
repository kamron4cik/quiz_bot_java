package uz.quizplatform.quizservice.application.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class StartQuizRequest {
    private Long userId;
    private UUID categoryId;
    private String mode;
    private int questionCount;
    private int timePerQuestionSeconds;
    private int questionOffset;
}
