package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserDTO> create(
            @RequestBody CreateUserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.status(201).body(toDTO(user));
    }

    @GetMapping("/{userId}")
    public UserDTO getById(@PathVariable Long userId) {
        return toDTO(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND)));
    }

    // Auth login
    @GetMapping("/by-email")
    public UserAuthDTO getByEmail(
            @RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND));
        return new UserAuthDTO(
                user.getUserId(),
                user.getPasswordHash());
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getDisplayName(),
                user.getEmail());
    }
}