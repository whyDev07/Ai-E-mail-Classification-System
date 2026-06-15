package com.ai_emailclassifier.serviceTest;

import com.ai_emailclassifier.dto.request.LoginRequest;
import com.ai_emailclassifier.dto.request.RegisterRequest;
import com.ai_emailclassifier.dto.response.AuthResponse;
import com.ai_emailclassifier.entity.User;
import com.ai_emailclassifier.repository.UserRepository;
import com.ai_emailclassifier.security.JwtUtil;
import com.ai_emailclassifier.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("dev");
        request.setPassword("password123");
        request.setRole(User.Role.USER);

        when(userRepository.existsByUsername(request.getUsername()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        when(jwtUtil.generateToken(any(User.class)))
                .thenReturn("jwt-token");


        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("dev", response.getUsername());
        assertEquals("USER", response.getRole());


        verify(userRepository).existsByUsername("dev");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("dev");
        request.setPassword("password123");
        request.setRole(User.Role.USER);

        when(userRepository.existsByUsername("dev"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "Username already exists: dev",
                exception.getMessage()
        );

        verify(userRepository).existsByUsername("dev");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();
        request.setUsername("dev");
        request.setPassword("password123");

        User user = User.builder()
                .username("dev")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();

        when(userRepository.findByUsername("dev"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(user))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("dev", response.getUsername());
        assertEquals("USER", response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("dev");
        verify(jwtUtil).generateToken(user);
    }


    @Test
    void shouldThrowWhenUserNotFoundDuringLogin() {

        LoginRequest request = new LoginRequest();
        request.setUsername("dev");
        request.setPassword("password123");

        when(userRepository.findByUsername("dev"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("dev");
        verify(jwtUtil, never()).generateToken(any());
    }

}