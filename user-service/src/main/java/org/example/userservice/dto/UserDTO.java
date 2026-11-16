package org.example.userservice.dto;

public record UserDTO(
        Long   userId,
        String displayName,
        String email
) {}