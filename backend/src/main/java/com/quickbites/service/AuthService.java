package com.quickbites.service;

import com.quickbites.config.AuthProperties;
import com.quickbites.dto.LoginResponse;
import com.quickbites.exception.UnauthorizedException;
import com.quickbites.model.User;
import com.quickbites.repository.UserRepository;
import com.quickbites.util.PasswordHasher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private record Session(Long userId, Instant expiresAt) {
    }

    private final UserRepository userRepository;
    private final Duration ttl;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.ttl = Duration.ofMinutes(authProperties.tokenTtlMinutes());
    }

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(u -> PasswordHasher.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user.getId(), Instant.now().plus(ttl)));
        return new LoginResponse(token, user.getId(), user.getFullName(), user.getEmail());
    }

    public void logout(String token) {
        sessions.remove(token);
    }

    public Optional<Long> resolveUserId(String token) {
        Session session = sessions.get(token);
        if (session == null) return Optional.empty();
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session.userId());
    }
}
