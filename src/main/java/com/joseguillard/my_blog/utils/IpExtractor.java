package com.joseguillard.my_blog.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility for extracting the real client IP address from the HTTP request
 * <p>
 * Relies on ForwardedHeaderFilter (registered in WebConfig) to unwrap
 * X-Forwarded-For headers from trusted proxies before this method is called.
 * That filter rewrites request.getRemoteAddr() to the real client IP,
 * so manual header parsing here is unnecessary and unsafe, a client
 * could otherwise spoof X-Forwarded-For to bypass IP-based rate limiting.
 */
public class IpExtractor {

    private IpExtractor() {}

    /**
     * Returns the real client IP address.
     * ForwardedHeaderFilter ensures this reflects the originating client,
     * not the proxy, when running behind a reverse proxy
     */
    public static String extractClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
