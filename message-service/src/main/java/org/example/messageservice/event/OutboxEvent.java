package org.example.messageservice.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {
    @Id
    private UUID eventId;
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    public OutboxEvent(UUID eventId, String payload) {
        this.eventId = eventId;
        this.payload = payload;
    }

    public enum OutboxStatus {PENDING, PROCESSED, FAILED}
}
