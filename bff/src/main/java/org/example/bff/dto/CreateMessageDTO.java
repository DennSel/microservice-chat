package org.example.bff.dto;

public record CreateMessageDTO(
        String channelId,
        Long senderId,
        String body
) {}