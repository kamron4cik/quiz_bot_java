package uz.quizplatform.questionimportservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedQuestion {

    private UUID id;
    private UUID jobId;

    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String explanation;

    @Builder.Default
    private boolean isValid = true;
    
    @Builder.Default
    private boolean isDuplicate = false;
    
    private String validationError;
    private Integer sourceLineNumber;

    public void markInvalid(String error) {
        this.isValid = false;
        this.validationError = error;
    }

    public void markDuplicate() {
        this.isDuplicate = true;
    }
}
