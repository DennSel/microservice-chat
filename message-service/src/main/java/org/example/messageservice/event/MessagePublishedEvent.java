package org.example.messageservice.event;

import java.util.UUID;

public record MessagePublishedEvent(
        UUID eventId,
        Long   messageId,
        String channelId,
        Long   senderId,
        String body
) {}
