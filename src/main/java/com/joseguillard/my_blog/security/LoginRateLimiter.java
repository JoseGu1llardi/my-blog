package com.joseguillard.my_blog.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;

    private final Cache<String, Integer> loginAttempts;

    public LoginRateLimiter() {
        this(Duration.ofMinutes(1));
    }

    LoginRateLimiter(Duration ttl) {
        this.loginAttempts = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(100_000)
                .build();
    }

    public boolean isAllowed(String ipAddress) {
        // getIfPresent returns null if expired or never seen - Caffeine handles TTL
        Integer attempts = loginAttempts.getIfPresent(ipAddress);

        if (attempts == null) {
            // First attempt in this window
            loginAttempts.put(ipAddress, 1);
            return true;
        }

        if (attempts < MAX_ATTEMPTS) {
            loginAttempts.put(ipAddress, attempts + 1);
            return true;
        }

        // Limit exceeded
        return false;
    }
}
