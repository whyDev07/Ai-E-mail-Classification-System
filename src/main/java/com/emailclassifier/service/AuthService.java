package com.emailclassifier.service;

import com.emailclassifier.dto.request.LoginRequest;
import com.emailclassifier.dto.request.RegisterRequest;
import com.emailclassifier.dto.response.AuthResponse;
import com.emailclassifier.entity.User;
import com.emailclassifier.repository.UserRepository;
import com.emailclassifier.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    //By AuthenticationManager we don't manually compare passwords — Spring Security does it securely.
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                // encoding here (not in controller)? Service layer owns business rules
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
        log.info("Registered new user: {} with role: {}", user.getUsername(), user.getRole());

        // Generating token immediately — users can use the app right after registration
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // authenticate() will validate credentials via Spring Security's pipeline
        // Throws AuthenticationException automatically on bad credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword())
        );

        UserDetails user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtUtil.generateToken(user);
        log.info("User logged in: {}", request.getUsername());

        User userEntity = (User) user;
        return new AuthResponse(token, userEntity.getUsername(), userEntity.getRole().name());
    }
}