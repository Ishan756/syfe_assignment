package com.financemanager.config;

import com.financemanager.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void passwordEncoder_usesBcrypt() {
        SecurityConfig config = new SecurityConfig(mock(CustomUserDetailsService.class));
        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder.encode("password")).isNotBlank();
    }

    @Test
    void authenticationProvider_returnsDaoProvider() {
        SecurityConfig config = new SecurityConfig(mock(CustomUserDetailsService.class));

        DaoAuthenticationProvider provider = config.authenticationProvider();

        assertThat(provider).isNotNull();
    }
}