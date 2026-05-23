package com.financemanager.controller;

import com.financemanager.dto.request.LoginRequest;
import com.financemanager.dto.request.RegisterRequest;
import com.financemanager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @RestController: Marks this class as a Web Controller where every method returns data
 * (like JSON) directly in the HTTP response body instead of rendering a HTML template.
 * It combines @Controller and @ResponseBody.
 * 
 * @RequestMapping("/api/auth"): Sets the base URL path prefix for all endpoints in this controller.
 * Any request starting with "/api/auth" will be routed here.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Dependency Injection: Spring automatically injects our AuthService bean here.
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @PostMapping("/register"): Handles POST requests to "/api/auth/register".
     * 
     * @RequestBody: Tells Spring to parse the incoming JSON request body and map/bind it 
     * to a RegisterRequest Java object.
     * 
     * @Valid: Triggers Java Validation constraints (e.g. @NotBlank, @Email) defined inside 
     * the RegisterRequest class before the method is even executed. If validation fails, 
     * Spring returns a 400 Bad Request automatically.
     * 
     * ResponseEntity: A generic class representing the entire HTTP response (status code, headers, body).
     * We return status 201 Created on successful registration.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * @PostMapping("/login"): Handles POST requests to "/api/auth/login".
     * 
     * HttpServletRequest: We inject this so we can access the low-level HTTP Servlet request
     * to create and bind the user's session.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
        Map<String, String> response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response); // Returns HTTP Status 200 OK with the response body
    }

    /**
     * @PostMapping("/logout"): Handles POST requests to "/api/auth/logout".
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        Map<String, String> response = authService.logout(request);
        return ResponseEntity.ok(response);
    }
}
