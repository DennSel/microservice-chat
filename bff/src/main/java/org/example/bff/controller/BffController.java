package org.example.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.bff.dto.CreateMessageDTO;
import org.example.bff.dto.CreateUserDTO;
import org.example.bff.dto.LoginRequest;
import org.example.bff.dto.MessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bff")
@RequiredArgsConstructor
public class BffController {

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    @Value("${services.message-service.url}")
    private String messageServiceUrl;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserDTO dto) {
        return restTemplate.postForEntity(
                userServiceUrl + "/api/users", dto, Object.class);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dto) {
        return restTemplate.postForEntity(
                authServiceUrl + "/auth/login", dto, Object.class);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> postMessage(
            HttpServletRequest request,
            @RequestBody MessageRequest dto) {

        Long userId = (Long) request.getAttribute("userId");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());

        CreateMessageDTO outgoing =
                new CreateMessageDTO(dto.channelId(), userId, dto.body());

        return restTemplate.exchange(
                messageServiceUrl + "/api/messages",
                HttpMethod.POST,
                new HttpEntity<>(outgoing, headers),
                Object.class);
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestParam String channelId,
            @RequestParam(required = false) String since) {

        String url = messageServiceUrl + "/api/messages?channelId=" + channelId;
        if (since != null) {
            url += "&since=" + since;
        }

        return restTemplate.getForEntity(url, Object.class);
    }
}
