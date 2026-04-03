package com.joseguillard.my_blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

public class ViewRateLimiterTest {

    private ViewRateLimiter viewRateLimiter;

    @BeforeEach
    public void setUp() {
        // Use 100ms TTL so tests do not have to wait 30 minutes
        viewRateLimiter = new ViewRateLimiter(Duration.ofMillis(100));
    }

    @Test
    @DisplayName("Should return true on the first view")
    void shouldReturnTrueOnFirstView() {
        boolean result = viewRateLimiter.shouldIncrementView("192.168.1.1", "my-post");
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseOnSecondViewWithinWindow() {
        viewRateLimiter.shouldIncrementView("192.168.1.1", "my-post");

        boolean result = viewRateLimiter.shouldIncrementView("192.168.1.1", "my-post");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueAfterWindowExpires() throws InterruptedException {
        viewRateLimiter.shouldIncrementView("192.168.1.1", "my-post");

        // Wait for TTL to expire
        Thread.sleep(200);

        boolean result = viewRateLimiter.shouldIncrementView("192.168.1.1", "my-post");

        assertThat(result).isTrue();
    }
}
