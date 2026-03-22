package com.joseguillard.my_blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ViewRateLimiterTest {

    private ViewRateLimiter viewRateLimiter;
    private Map<String, Instant> views;
    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        viewRateLimiter = new ViewRateLimiter(clock);

        @SuppressWarnings("unchecked")
        Map<String, Instant> tempViews = (Map<String, Instant>)
                ReflectionTestUtils.getField(viewRateLimiter, "views");
        this.views = tempViews;
    }

    @Test
    @DisplayName("Should return true on the first view")
    void shouldReturnTrueOnFirstView() {
        String ipAddress = "192.168.1.1";
        String slug = "my-post";

        boolean result = viewRateLimiter.shouldIncrementView(ipAddress, slug);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseOnSecondViewWithinWindow() {
        String ipAddress = "192.168.1.1";
        String slug = "my-post";

        viewRateLimiter.shouldIncrementView(ipAddress, slug);

        boolean result = viewRateLimiter.shouldIncrementView(ipAddress, slug);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueAfterWindowExpires() {
        String ipAddress = "192.168.1.1";
        String slug = "my-post";

        String key = ipAddress + ":" + slug;

        views.put(key, Instant.now(clock).minus(Duration.ofMinutes(31)));
        boolean result = viewRateLimiter.shouldIncrementView(ipAddress, slug);

        assertThat(result).isTrue();
    }
}
