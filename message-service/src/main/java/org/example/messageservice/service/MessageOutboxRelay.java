package org.example.messageservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.messageservice.config.RabbitConfig;
import org.example.messageservice.event.OutboxEvent;
import org.example.messageservice.repository.OutboxRepository;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageOutboxRelay {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback((cd, ack, cause) -> {
            if (ack && cd != null) {
                UUID id = UUID.fromString(cd.getId());
                outboxRepository.findById(id).ifPresent(event -> {
                    event.setStatus(OutboxEvent.OutboxStatus.PROCESSED);
                    outboxRepository.save(event);
                });
                log.info("Event {} acknowledged", cd.getId());
            }
        });
    }

    @Scheduled(fixedDelay = 5000)
    public void relayEvents() {
        for (OutboxEvent event :
                outboxRepository.findByStatus(OutboxEvent.OutboxStatus.PENDING)) {
            try {
                CorrelationData cd =
                        new CorrelationData(event.getEventId().toString());
                rabbitTemplate.convertAndSend(
                        RabbitConfig.EXCHANGE, RabbitConfig.ROUTING,
                        event.getPayload(), cd);
                log.info("Relaying event {}", event.getEventId());
            } catch (Exception e) {
                log.error("Relay failed for {}: {}", event.getEventId(), e.getMessage());
            }
        }
    }
}