package com.joseguillard.my_blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String baseUrl, Cors cors) {

    // Bound via @ConfigurationProperties because @Value cannot read YAML lists
    // (they are flattened to allowed-origins[0], [1], ...). Accepts both a YAML
    // list and a comma-separated string.
    public record Cors(List<String> allowedOrigins) {
    }
}
