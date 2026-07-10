package uz.quizplatform.questionservice.application.dto.response;

import uz.quizplatform.questionservice.domain.entity.Question;
import uz.quizplatform.questionservice.domain.valueobject.Difficulty;

import java.time.Instant;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID categoryId,
        String text,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        int correctAnswer,
        String explanation,
        String imageUrl,
        Difficulty difficulty,
        boolean active,
        Instant createdAt
) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getCategoryId(),
                question.getText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectAnswer(),
                question.getExplanation(),
                question.getImageUrl(),
                question.getDifficulty(),
                question.isActive(),
                question.getCreatedAt()
        );
    }
}
