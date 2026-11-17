package org.example.messageservice.dto;

import java.time.LocalDateTime;

public record MessageDTO(
        Long messageId,
        String channelId,
        Long senderId,
        String senderName,
        String body,
        LocalDateTime createdAt
) {
}