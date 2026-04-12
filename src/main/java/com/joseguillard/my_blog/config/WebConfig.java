package com.joseguillard.my_blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Web infrastructure configuration.
 * <p>
 * Registers Spring's ForwardedHeaderFilter so the application correctly
 * resolves the real client IP and protocol when running behind a reverse
 * proxy (nginx, AWS ALB, Railway). Without this filter, request.getRemoteAddr()
 * always returns proxy's IP, making IP-based rate limiting ineffective.
 */
@Configuration
public class WebConfig {

    /**
     * Unwraps X-Forwarded-For, X-Forwarded-Proto, and related headers
     * set by trusted upstream proxies, rewriting the request so all
     * downstream components (filters, controllers) see the real client values.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
