package com.joseguillard.my_blog.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Should extract a username from a valid token")
    void shouldExtractUsernameFromToken() {
        // Generate a real token first, then verify if we can read the subject back.
        // This tests the round-trip: generate -> extract -> same username
        String token = jwtService.generateToken("grillard", 1);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("grillard");
    }

    @Test
    @DisplayName("Should extract tokenVersion claim from token")
    void shouldExtractTokenVersionFromToken() {
        // Generate a real token first, then verify if the token version matches.
        String token = jwtService.generateToken("grillard", 1);

        Integer tokenVersion = jwtService.extractTokenVersion(token);

        assertThat(tokenVersion).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return null when tokenVersion is absent from token")
    void shouldReturnNullWhenTokenVersionClaimIsAbsent() {
        SecretKey signingKey = (SecretKey) ReflectionTestUtils.getField(jwtService, "signingKey");

        String tokenWithoutVersion = Jwts.builder()
                .subject("grillard")
                .issuer("my-blog-api")
                .audience().add("my-blog-client").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(signingKey)
                .compact();

        assertThat(jwtService.extractTokenVersion(tokenWithoutVersion)).isNull();
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
    @DisplayName("Should return false when username does not match token")
    void shouldReturnFalseWhenUsernameDoesNotMatch() {
        // Generate a token for "grillard" but validates against a different user
        // isTokenValid() compares extracted username from token vs. userDetails.getUsername()
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

    @Test
    @DisplayName("Should throw IllegalArgumentException when secret is shorter than 32 bytes")
    void shouldRejectWeakSecret() {
        assertThatThrownBy(() -> new JwtService("dGVzdA==", 3600000L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw JwtException when token is signed with a different key")
    void shouldThrowJwtExceptionWhenTokenSignedWithDifferentKey() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("dGVzdC1vbmx5LW5vdC1hLXJlYWwtc2VjcmV0LWRvLW5vdC11c2UtaW4tcHJvZA=="));

        String tokenWithTamperedKey = Jwts.builder()
                .subject("grillard")
                .issuer("my-blog-api")
                .audience().add("my-blog-client").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(wrongKey)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(tokenWithTamperedKey))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should throw JwtException when token has wrong issuer")
    void shouldThrowJwtExceptionWhenTokenHasWrongIssuer() {
        SecretKey signingKey = (SecretKey) ReflectionTestUtils.getField(jwtService, "signingKey");

        String tokenWithDifferentIssuer = Jwts.builder()
                .subject("grillard")
                .issuer("different-issuer")
                .audience().add("my-blog-client").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(signingKey)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(tokenWithDifferentIssuer))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should throw JwtException when token has wrong audience")
    void shouldThrowJwtExceptionWhenTokenHasWrongAudience() {
        SecretKey signingKey = (SecretKey) ReflectionTestUtils.getField(jwtService, "signingKey");

        String tokenWithDifferentAudience = Jwts.builder()
                .subject("grillard")
                .issuer("my-blog-api")
                .audience().add("different-audience").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(signingKey)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(tokenWithDifferentAudience))
                .isInstanceOf(JwtException.class);
    }
}
