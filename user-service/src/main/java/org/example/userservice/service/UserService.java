package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.CreateUserDTO;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User createUser(CreateUserDTO dto) {
        User user = new User();
        user.setDisplayName(dto.displayName());
        user.setEmail(dto.email());
        user.setPasswordHash(
                passwordEncoder.encode(dto.password()));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}