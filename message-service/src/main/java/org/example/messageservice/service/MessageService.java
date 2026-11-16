package org.example.messageservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.messageservice.entity.Message;
import org.example.messageservice.event.MessagePublishedEvent;
import org.example.messageservice.event.OutboxEvent;
import org.example.messageservice.repository.MessageRepository;
import org.example.messageservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final OutboxRepository outboxRepository;
    private final JsonMapper objectMapper;

    @Transactional
    public Message postMessage(String channelId, Long senderId, String body) {

        Message saved = messageRepository.save(new Message(channelId, senderId, body));

        UUID eventId = UUID.randomUUID();

        MessagePublishedEvent event = new MessagePublishedEvent(eventId, saved.getMessageId(), channelId, senderId, body);

        outboxRepository.save(new OutboxEvent(eventId, objectMapper.writeValueAsString(event)));

        return saved;
    }
}