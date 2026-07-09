package uz.quizplatform.common.domain.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends QuizPlatformException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
