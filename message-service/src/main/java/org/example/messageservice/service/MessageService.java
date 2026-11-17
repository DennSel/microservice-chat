package org.example.messageservice.service;

import lombok.RequiredArgsConstructor;
import org.example.grpc.GetUserRequest;
import org.example.grpc.UserServiceGrpc;
import org.example.messageservice.dto.MessageDTO;
import org.example.messageservice.entity.Message;
import org.example.messageservice.event.MessagePublishedEvent;
import org.example.messageservice.event.OutboxEvent;
import org.example.messageservice.repository.MessageRepository;
import org.example.messageservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final OutboxRepository outboxRepository;
    private final JsonMapper objectMapper;
    private final UserServiceGrpc.UserServiceBlockingStub userGrpcStub;

    // TEMPORARY! senderId is client-supplied, impersonation possible.
    // Replace with trusted X-User-Id header from the BFF when implemented.
    @Transactional
    public MessageDTO postMessage(String channelId, Long senderId, String body) {

        Message saved = messageRepository.save(new Message(channelId, senderId, body));

        UUID eventId = UUID.randomUUID();

        MessagePublishedEvent event = new MessagePublishedEvent(
                eventId, saved.getMessageId(), channelId, senderId, body);

        outboxRepository.save(new OutboxEvent(
                eventId, objectMapper.writeValueAsString(event)));

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(String channelId, LocalDateTime since) {
        List<Message> messages = (since != null)
                ? messageRepository.findByChannelIdAndCreatedAtAfter(channelId, since)
                : messageRepository.findByChannelId(channelId);
        return messages.stream().map(this::toDTO).toList();
    }

    private MessageDTO toDTO(Message message) {
        String senderName = userGrpcStub.getUser(GetUserRequest.newBuilder().setUserId(message.getSenderId()).build()).getDisplayName();

        return new MessageDTO(
                message.getMessageId(),
                message.getChannelId(),
                message.getSenderId(),
                senderName,
                message.getBody(),
                message.getCreatedAt());
    }
}