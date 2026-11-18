package org.example.messageservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.messageservice.dto.CreateMessageDTO;
import org.example.messageservice.dto.MessageDTO;
import org.example.messageservice.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Trust the identity from the BFF when present, body senderId is
    // only a fallback for direct tests against this service (bypassing the BFF).
    @PostMapping
    public ResponseEntity<MessageDTO> post(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody CreateMessageDTO dto) {

        Long senderId = (headerUserId != null)
                ? headerUserId : dto.senderId();

        return ResponseEntity.status(201)
                .body(messageService.postMessage(
                        dto.channelId(), senderId, dto.body()));
    }

    @GetMapping
    public List<MessageDTO> getMessages(
            @RequestParam String channelId,
            @RequestParam(required = false) LocalDateTime since) {
        return messageService.getMessages(channelId, since);
    }
}