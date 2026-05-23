package com.financemanager.controller;

import com.financemanager.dto.request.LoginRequest;
import com.financemanager.dto.request.RegisterRequest;
import com.financemanager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_returnsCreatedResponse() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("user@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("John Doe");
        registerRequest.setPhoneNumber("+1234567890");

        when(authService.register(registerRequest)).thenReturn(Map.of("message", "User registered successfully", "userId", 1L));

        var response = authController.register(registerRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).containsEntry("message", "User registered successfully");
        verify(authService).register(registerRequest);
    }

    @Test
    void login_returnsOkResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("password123");

        when(authService.login(loginRequest, request)).thenReturn(Map.of("message", "Login successful"));

        var response = authController.login(loginRequest, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("message", "Login successful");
        verify(authService).login(loginRequest, request);
    }

    @Test
    void logout_returnsOkResponse() {
        when(authService.logout(request)).thenReturn(Map.of("message", "Logout successful"));

        var response = authController.logout(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("message", "Logout successful");
        verify(authService).logout(request);
    }
}