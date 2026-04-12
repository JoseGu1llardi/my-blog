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

    public void recordFailure(String ipAddress) {
        Integer attempts = loginAttempts.getIfPresent(ipAddress);
        loginAttempts.put(ipAddress, attempts == null ? 1 : attempts + 1);
    }

    public void recordSuccess(String ipAddress) {
        loginAttempts.invalidate(ipAddress); // reset counter on successful login
    }

    public boolean isBlocked(String ipAddress) {
        Integer attempts = loginAttempts.getIfPresent(ipAddress);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }
}
