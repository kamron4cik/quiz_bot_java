package uz.quizplatform.common.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the quiz platform.
 * Every event carries a unique ID, timestamp, and correlation ID for distributed tracing.
 */
@Getter
public abstract class DomainEvent {

    private final String eventId;
    private final String eventType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant occurredAt;

    private final String correlationId;
    private final String serviceName;

    protected DomainEvent(String eventType, String correlationId, String serviceName) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = Instant.now();
        this.correlationId = correlationId;
        this.serviceName = serviceName;
    }
}
