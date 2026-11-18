package org.example.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.TokenResponse;
import org.example.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest req) {
        return ResponseEntity.ok(new TokenResponse(
                authService.login(req.email(), req.password())));
    }
}
