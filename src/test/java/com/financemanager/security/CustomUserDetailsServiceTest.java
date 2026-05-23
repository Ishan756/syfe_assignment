package com.financemanager.security;

import com.financemanager.entity.User;
import com.financemanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_returnsSpringUser() {
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("hashed-password");
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));

        var userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
    }

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(userRepository.findByUsername("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}