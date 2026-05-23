package com.financemanager.service;

import com.financemanager.dto.request.LoginRequest;
import com.financemanager.dto.request.RegisterRequest;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ConflictException;
import com.financemanager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Service: Marks this class as a Service component in Spring's Application Context.
 * The Service layer contains our business logic, orchestrating calls to repositories,
 * performing data validation, and handling transactions.
 */
@Service
public class AuthService {

    // Dependency Injection fields. Marked final to ensure they are immutable and initialized via the constructor.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructor Dependency Injection: Spring automatically finds the beans for
     * UserRepository, PasswordEncoder, and AuthenticationManager, and injects them here.
     * This is the recommended way (instead of using @Autowired on fields) because it makes the
     * class easy to test and ensures dependencies cannot be null.
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user in the system.
     * 1. Checks if the email/username already exists (throws a 409 Conflict if yes).
     * 2. Encrypts the plain text password using PasswordEncoder (BCrypt).
     * 3. Saves the User entity to the database.
     */
    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            // Throwing custom exception which will be intercepted by our GlobalExceptionHandler and returned as a 409 status.
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // CRITICAL: Hash the password before saving to database!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        // Saves user to PostgreSQL and returns the saved user entity (which now contains the auto-generated ID)
        User saved = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("userId", saved.getId());
        return response;
    }

    /**
     * Login authentication process.
     * 1. Authenticates user credentials using AuthenticationManager.
     * 2. If valid, stores authentication token in SecurityContextHolder.
     * 3. Binds the security context to the HTTP Session so Spring Security can recognize subsequent requests as authenticated.
     */
    public Map<String, String> login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Step A: Attempt authentication using username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Step B: If successful, create a new Security Context and set the authenticated token
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            
            // Step C: Set the global context for the current executing thread
            SecurityContextHolder.setContext(context);

            // Step D: Retrieve/create a session and store the Spring Security Context in it.
            // This ensures subsequent requests sent with the JSESSIONID cookie remain authenticated.
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        } catch (BadCredentialsException ex) {
            // BadCredentialsException is thrown by Spring Security when username/password doesn't match
            throw new BadRequestException("Invalid username or password");
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        return response;
    }

    /**
     * Logout process.
     * 1. Invalidates the HTTP Session (which deletes the session state and JSESSIONID on server side).
     * 2. Clears the security context from the current thread.
     */
    public Map<String, String> logout(HttpServletRequest request) {
        // Find existing session without creating a new one
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Destroy session
        }
        
        // Clear authentication from SecurityContextHolder
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return response;
    }

    /**
     * Helper method to get the currently logged-in user entity from the active security context.
     * Useful for associating transactions/categories with the correct user.
     */
    public User getCurrentUser() {
        // Retrieve the authenticated user's username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Retrieve user entity from the database
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
