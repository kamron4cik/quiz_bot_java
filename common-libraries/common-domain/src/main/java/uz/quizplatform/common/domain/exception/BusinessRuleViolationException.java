package uz.quizplatform.common.domain.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends QuizPlatformException {
    public BusinessRuleViolationException(String message, String errorCode) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, errorCode);
    }
}
