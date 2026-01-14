package com.joseguillard.my_blog.model.vo;

import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record Email(String address) {
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^(?i)[a-z0-9._%+-]+@(?:[a-z0-9-]+\\.)+[a-z]{2,}$");

    // Compact constructor for validation
    public Email {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail address cannot be null or empty");
        }

        address = address.toLowerCase().trim();

        if (!EMAIL_PATTERN.matcher(address).matches()) {
            throw new IllegalArgumentException("Invalid e-mail: " + address);
        }
    }
}
