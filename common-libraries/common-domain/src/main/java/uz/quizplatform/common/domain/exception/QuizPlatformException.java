package uz.quizplatform.common.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all platform-specific exceptions.
 * Carries an HTTP status and a machine-readable error code.
 */
public abstract class QuizPlatformException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected QuizPlatformException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected QuizPlatformException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
}
