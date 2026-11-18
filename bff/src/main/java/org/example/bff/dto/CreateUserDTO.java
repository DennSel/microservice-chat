package org.example.bff.dto;

public record CreateUserDTO(
        String displayName,
        String email,
        String password
) {}