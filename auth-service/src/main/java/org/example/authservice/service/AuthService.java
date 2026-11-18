package org.example.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.authservice.dto.UserAuthDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class AuthService {

    private static final String DUMMY_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${services.user-service.url}")
    private String userServiceUrl;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String login(String email, String password) {
        // Get userId and passwordHash
        // Don't reveal if mail exists, masks with 401 to client when 404 from User Service
        UserAuthDTO user;
        try {
            user = restTemplate.getForObject(
                    userServiceUrl + "/api/users/by-email?email=" + email,
                    UserAuthDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Email not found gives 401, same as wrong password.
            // Dummy BCrypt match makes this path take as long as the wrong-password path below, hiding which one failed.
            // Rate-limiting would be best practice, but this is a simple fix for this project.
            passwordEncoder.matches(password, DUMMY_HASH);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // A defensive check for 200 with empty body from User Service, should not happen but now gets 401.
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // Verify password against hash
        if (!passwordEncoder.matches(password, user.passwordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // Sign and return JWT
        return generateToken(user.userId());
    }


     // Builds and signs JWT for the given user
    private String generateToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(86_400)))
                .signWith(getSigningKey())
                .compact();
    }

    // Turns the jwt.secret property string into a HMAC-SHA256 signing key.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

     // Verifies the token's signature and expiration, and returns the user id.
     // Throws an exception if the token is invalid, tampered with, or expired.
    public Long validateToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}