package uz.quizplatform.common.domain.exception;

import org.springframework.http.HttpStatus;

/** Raised when a user tries to start a quiz but already has an active session. */
public class ActiveQuizSessionExistsException extends QuizPlatformException {
    public ActiveQuizSessionExistsException(Long userId) {
        super("User " + userId + " already has an active quiz session", HttpStatus.CONFLICT, "ACTIVE_QUIZ_EXISTS");
    }
}
