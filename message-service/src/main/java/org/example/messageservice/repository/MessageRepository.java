package org.example.messageservice.repository;

import org.example.messageservice.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    List<Message> findByChannelId(
            String channelId);

    List<Message> findByChannelIdAndCreatedAtAfter(
            String channelId, LocalDateTime since);
}