package com.joseguillard.my_blog.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpExtractor {

    private IpExtractor() {}

    public static String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // First IP in the chain is the real client
            // Proxy appends its own IP at the end
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
