package com.financemanager.config;

import com.financemanager.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @Configuration: Marks this class as a source of bean definitions. Spring will process
 * this class and generate Spring Beans that can be used elsewhere in the application.
 * 
 * @EnableWebSecurity: Tells Spring Boot to enable Spring Security's web security support 
 * and integrate it with Spring MVC.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CustomUserDetailsService is responsible for fetching user details from the database.
    private final CustomUserDetailsService userDetailsService;

    // Dependency Injection: Spring automatically injects the CustomUserDetailsService bean here.
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * The SecurityFilterChain is a chain of servlet filters that intercept incoming HTTP requests
     * and apply security rules (authentication, authorization, session rules, etc.).
     * 
     * @Bean: Registers this method's return value (SecurityFilterChain) as a Spring Bean so Spring
     * can use it to secure our REST endpoints.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF (Cross-Site Request Forgery): Disabled because this is a stateless/REST API
            // where authentication tokens/sessions are validated manually, and we don't rely purely on 
            // automatic browser form submissions.
            .csrf(csrf -> csrf.disable())
            
            // 2. Authorization Rules: Defining who is allowed to access which URL path.
            .authorizeHttpRequests(auth -> auth
                // Allow anyone to access the Register and Login POST endpoints without authenticating.
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                // Every other request to any endpoint MUST be authenticated (logged in).
                .anyRequest().authenticated()
            )
            
            // 3. Session Management: Define how Spring Security tracks logged-in users.
            // SessionCreationPolicy.IF_REQUIRED: Spring Security will only create a HTTP session
            // if it is needed (e.g. when a user logs in, a session cookie JSESSIONID is created).
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // 4. Exception Handling: Customize how Spring Security responds when access is denied.
            .exceptionHandling(ex -> ex
                // AuthenticationEntryPoint: Triggered when an unauthenticated user tries to access a protected endpoint (returns 401).
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"message\": \"Unauthorized\"}");
                })
                // AccessDeniedHandler: Triggered when a logged-in user lacks permissions/roles for an endpoint (returns 403).
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"message\": \"Access denied\"}");
                })
            )
            
            // 5. Logout: We disable the default Spring Security /logout endpoint because we implement
            // custom logout behavior inside our AuthService to clear sessions manually.
            .logout(logout -> logout.disable());

        return http.build();
    }

    /**
     * PasswordEncoder: Used to securely hash and verify user passwords.
     * We use BCryptPasswordEncoder, which is a strong, industry-standard hashing algorithm.
     * Never store plain text passwords in the database!
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider: An authentication provider that uses a UserDetailsService 
     * and a PasswordEncoder to retrieve and validate user passwords from a database.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // Load user details
        provider.setPasswordEncoder(passwordEncoder());    // Verify the hashed password
        return provider;
    }

    /**
     * AuthenticationManager: The main coordinator interface for authentication in Spring Security.
     * We expose it as a Bean so our custom AuthService can invoke it to authenticate users during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
