package com.quickbites.controller;

import com.quickbites.dto.LoginRequest;
import com.quickbites.dto.LoginResponse;
import com.quickbites.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        authService.logout(header.replaceFirst("^Bearer ", "").trim());
        return ResponseEntity.noContent().build();
    }
}
