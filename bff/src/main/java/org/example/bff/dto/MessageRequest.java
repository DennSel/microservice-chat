package org.example.bff.dto;

public record MessageRequest(
        String channelId,
        String body
) {}