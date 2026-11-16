package org.example.userservice.dto;

public record CreateUserDTO(
        String displayName,
        String email,
        String password
) {}