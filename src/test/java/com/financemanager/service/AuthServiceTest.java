package com.financemanager.service;

import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ConflictException;
import com.financemanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByUsername("a@b.com")).thenReturn(true);

        com.financemanager.dto.request.RegisterRequest req = new com.financemanager.dto.request.RegisterRequest();
        req.setUsername("a@b.com");
        req.setPassword("pass123");
        req.setFullName("A B");
        req.setPhoneNumber("+100");

        assertThatThrownBy(() -> authService.register(req)).isInstanceOf(ConflictException.class);
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("new@x.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");

        User saved = new User();
        saved.setId(123L);
        saved.setUsername("new@x.com");

        when(userRepository.save(any())).thenReturn(saved);

        com.financemanager.dto.request.RegisterRequest req = new com.financemanager.dto.request.RegisterRequest();
        req.setUsername("new@x.com");
        req.setPassword("pass123");
        req.setFullName("New User");
        req.setPhoneNumber("+100");

        java.util.Map<String, Object> res = authService.register(req);

        assertThat(res).containsEntry("message", "User registered successfully");
        assertThat(res).containsKey("userId");
    }

    @Test
    void login_badCredentialsThrows() {
        com.financemanager.dto.request.LoginRequest req = new com.financemanager.dto.request.LoginRequest();
        req.setUsername("u@x.com");
        req.setPassword("bad");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(req, httpRequest)).isInstanceOf(BadRequestException.class);
    }
}
