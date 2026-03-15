package com.joseguillard.my_blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void setup() {

        // JwtService uses @Value to read secretKey and expiration from application.yml
        // In a unit test, Spring context doesn't run so @Value fields are never injected.
        // ReflectionTestUtils let us set private fields directly by name - bypassing
        // the need for Spring to inject them. This is the standard pattern for testing
        // Spring services in isolation
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "bXktYmxvZy1kZXYtc2VjcmV0LWtleS1mb3ItbG9jYWwtZGV2LW9ubHktY2hhbmdlLW1l");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    @DisplayName("Should generate a non-null token from a username")
    void shouldGenerateTokenFromUsername() {
        // generateToken() builds a JWT signed with our secret key.
        // We verify it returns something non-null and non-blank
        // the internal format (header.payload.signature) is jjwt's responsibility,
        // not ours to test.
        String token = jwtService.generateToken("grillard");

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();

        // A JWT always has exactly 2 dots separating the 3 parts
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract an username from a valid token")
    void shouldExtractUsernameFromToken() {
        // Generate a real token first, then verify if we can read the subject back.
        // This tests the round-trip: generate -> extract -> same username
        String token = jwtService.generateToken("grillard");

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("grillard");
    }

    @Test
    @DisplayName("Should return true when token is valid and username matches")
    void shouldReturnTrueWhenTokenIsValid() {
        // UserDetails is the interface Spring Security uses to represent a user.
        // User.withUserName() is a builder from spring-security-core that creates
        // a simple in-memory UserDetails - we don't need a real Author entity here
        // because isTokenValid() only calls userDetails.getUsername().
        UserDetails userDetails = User.withUsername("grillard")
                .password("grillard")
                .authorities(Collections.emptySet())
                .build();

        String token = jwtService.generateToken("grillard");

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }
}
