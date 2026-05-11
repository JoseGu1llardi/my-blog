package com.joseguillard.my_blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginRateLimiterTest {

    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiter(Duration.ofMinutes(1));
    }

    // 4 failures → NOT blocked
    @Test
    @DisplayName("Should not block after 4 failures")
    void shouldNotBlockAfterFourFailures() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("127.0.0.1");
        }

        assertThat(limiter.isBlocked("127.0.0.1")).isFalse();
    }

    // 5 failures → blocked
    @Test
    @DisplayName("Should block after 5 failures")
    void shouldBlockAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("127.0.0.1");
        }

        assertThat(limiter.isBlocked("127.0.0.1")).isTrue();
    }

    // 4 failures + success → NOT blocked (reset)
    @Test
    @DisplayName("Should reset counter after successful login")
    void shouldResetCounterAfterSuccessfulLogin() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("127.0.0.1");
        }

        assertThat(limiter.isBlocked("127.0.0.1")).isTrue();

        limiter.recordSuccess("127.0.0.1");

        assertThat(limiter.isBlocked("127.0.0.1")).isFalse();
    }

    // 5 failures + TTL expired → NOT blocked
    @Test
    @DisplayName("Should not block after TTL expires")
    void shouldUnblockAfterTtlExpires() throws InterruptedException {}

    // different IPs → independent counters
    @Test
    @DisplayName("Should track each IP independently")
    void shouldTrackEachIpIndependently() {}
}
