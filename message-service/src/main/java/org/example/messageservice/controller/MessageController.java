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

    // TEMPORARY! senderId is client-supplied, impersonation possible.
    // Replaced by trusted X-User-Id header when BFF is implemented.
    @PostMapping
    public ResponseEntity<MessageDTO> post(@RequestBody CreateMessageDTO dto) {
        return ResponseEntity.status(201)
                .body(messageService.postMessage(
                        dto.channelId(), dto.senderId(), dto.body()));
    }

    @GetMapping
    public List<MessageDTO> getMessages(
            @RequestParam String channelId,
            @RequestParam(required = false) LocalDateTime since) {
        return messageService.getMessages(channelId, since);
    }
}