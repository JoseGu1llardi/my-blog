package com.joseguillard.my_blog.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Clock clock;
    private final Map<String, AttemptData> attempts = new ConcurrentHashMap<>();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean isAllowed(String ipAddress) {
        Instant now = Instant.now(clock);
        AttemptData data = attempts.get(ipAddress);

        // No previous attempts or window has expired - reset
        if (data == null || now.isAfter(data.windowStart.plus(WINDOW))) {
            attempts.put(ipAddress, new AttemptData(1, now));
            return true;
        }

        // Within window - check count
        if (data.count < MAX_ATTEMPTS) {
            attempts.put(ipAddress, new AttemptData(data.count + 1, data.windowStart()));
            return true;
        }

        return false;
    }

    private record AttemptData(int count, Instant windowStart) {}
}
