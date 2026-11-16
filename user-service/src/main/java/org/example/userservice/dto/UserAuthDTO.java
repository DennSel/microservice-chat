package org.example.userservice.dto;

public record UserAuthDTO(
        Long userId,
        String passwordHash
) {}