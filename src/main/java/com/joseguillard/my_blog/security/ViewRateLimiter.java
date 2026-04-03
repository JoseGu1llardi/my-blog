package com.joseguillard.my_blog.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ViewRateLimiter {

    private final Cache<String, Boolean> views;

    // Production constructor
    public ViewRateLimiter() {
        this(Duration.ofMinutes(30));
    }

    // Testable constructor - test call this with short ttl
    ViewRateLimiter(Duration ttl) {
        this.views = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(100_000)
                .build();
    }

    public boolean shouldIncrementView(String ipAddress, String slug) {
        String key = ipAddress + ":" + slug;

        // getIfPresent returns null if not in cache (expired or never seen)
        if (views.getIfPresent(key) != null) {
            return false; // already viewed within window
        }

        views.put(key, Boolean.TRUE);
        return true;
    }
}
