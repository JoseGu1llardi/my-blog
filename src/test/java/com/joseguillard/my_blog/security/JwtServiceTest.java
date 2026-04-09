package com.joseguillard.my_blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
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
        jwtService = new JwtService(
                "bXktYmxvZy1kZXYtc2VjcmV0LWtleS1mb3ItbG9jYWwtZGV2LW9ubHktY2hhbmdlLW1l",
                3600000L);
    }

    @Test
    @DisplayName("Should generate a non-null token from a username")
    void shouldGenerateTokenFromUsername() {
        // generateToken() builds a JWT signed with our secret key.
        // We verify it returns something non-null and non-blank
        // the internal format (header.payload.signature) is jjwt's responsibility,
        // not ours to test.
        String token = jwtService.generateToken("grillard", 1);

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
        String token = jwtService.generateToken("grillard", 1);

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

        String token = jwtService.generateToken("grillard", 1);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false when uername does not match token")
    void shouldReturnFalseWhenUsernameDoesNotMatch() {
        // Generate a token for "grillard" but validates against a different user
        // isTokenValid() compares extracted username from token vs userDetails.getUsername()
        // They will not match, so it should return false.
        UserDetails userDetails = User.withUsername("different-username")
                .password("password")
                .authorities(Collections.emptySet())
                .build();

        String token = jwtService.generateToken("grillard", 1);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when token is expired")
    void shouldReturnFalseWhenTokenIsExpired() {
        // Setting expiration to -1000L means the token expires 1 minute BEFORE
        // it was created - it is already expired at the moment of generation.
        // This is the trick for testing expiry without waiting for real time to pass
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);

        UserDetails userDetails = User.withUsername("grillard")
                .password("password")
                .authorities(Collections.emptySet())
                .build();

        String token = jwtService.generateToken("grillard", 1);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isFalse();
    }
}
