package com.ai_emailclassifier.controller;

import com.ai_emailclassifier.dto.request.LoginRequest;
import com.ai_emailclassifier.dto.request.RegisterRequest;
import com.ai_emailclassifier.dto.response.AuthResponse;
import com.ai_emailclassifier.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}