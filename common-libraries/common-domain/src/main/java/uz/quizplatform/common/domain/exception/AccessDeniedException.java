package uz.quizplatform.common.domain.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends QuizPlatformException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
