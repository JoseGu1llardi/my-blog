package com.joseguillard.my_blog.model.vo;

import com.joseguillard.my_blog.exception.InvalidEmailException;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

public record Email(String address) {
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^(?i)[a-z0-9._%+-]+@(?:[a-z0-9-]+\\.)+[a-z]{2,}$");

    // Compact constructor for validation
    public Email {
        if (address == null || address.trim().isEmpty()) {
            throw new InvalidEmailException("E-mail address cannot be null or empty");
        }

        address = address.toLowerCase().trim();

        if (!EMAIL_PATTERN.matcher(address).matches()) {
            throw new IllegalArgumentException("Invalid e-mail: " + address);
        }
    }

    // Factory method alternative
    public static Email of(String address) {
        return new Email(address);
    }

    public String getUser() {
        return address.substring(0, address.indexOf("@"));
    }

    public String getDomain() {
        return address.substring(address.indexOf("@") + 1);
    }
}
