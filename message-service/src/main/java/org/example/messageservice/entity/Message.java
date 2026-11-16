package org.example.messageservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;
    private String channelId;
    private Long senderId;
    private String body;
    private LocalDateTime createdAt;

    public Message(String channelId, Long senderId,
                   String body) {
        this.channelId = channelId;
        this.senderId = senderId;
        this.body = body;
        this.createdAt = LocalDateTime.now();
    }
}