package org.example.bff.dto;

public record LoginRequest(
        String email,
        String password
) {}