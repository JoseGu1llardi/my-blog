package com.joseguillard.my_blog.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViewRateLimiter {

    private final Clock clock;
    private final Map<String, Instant> views = new ConcurrentHashMap<>();

    public ViewRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /**
     * Enforces per-IP-per-slug 30-minute view rate limit
     */
    public boolean shouldIncrementView(String ipAddress, String slug) {
        String key = ipAddress + ":" + slug;
        Instant now = Instant.now(clock);

        Instant lastView = views.get(key);

        if (lastView == null || lastView.isBefore(now.minus(30, ChronoUnit.MINUTES))) {
            views.put(key, now);
            return true; // increment view count
        }
        return false; // skip
    }
}
