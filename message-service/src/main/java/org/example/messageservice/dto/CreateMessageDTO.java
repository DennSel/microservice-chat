package org.example.messageservice.dto;

public record CreateMessageDTO(
        String channelId,
        Long senderId,
        String body
) {
}