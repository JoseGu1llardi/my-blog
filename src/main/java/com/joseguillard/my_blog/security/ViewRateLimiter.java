package com.joseguillard.my_blog.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViewRateLimiter {

    private final Map<String, Instant> views = new ConcurrentHashMap<>();

    public boolean shouldIncrementView(String ipAddress, String slug) {
        String key = ipAddress + ":" + slug;
        Instant now = Instant.now();

        Instant lastView = views.get(key);

        if (lastView == null || lastView.isBefore(now.minus(30, ChronoUnit.MINUTES))) {
            views.put(key, now);
            return true; // increment view count
        }
        return false; // skip
    }
}
