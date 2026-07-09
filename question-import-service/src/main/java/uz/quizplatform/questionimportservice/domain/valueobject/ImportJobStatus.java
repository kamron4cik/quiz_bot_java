package uz.quizplatform.questionimportservice.domain.valueobject;

public enum ImportJobStatus {
    UPLOADED,
    PARSING,
    PREVIEW_READY,
    CONFIRMED,
    IMPORTING,
    COMPLETED,
    FAILED,
    CANCELLED
}
