package com.joseguillard.my_blog.model.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.text.Normalizer;

@Embeddable
public class Slug implements Serializable {
    private String value;

    protected Slug() {} // JPA requires empty constructor

    private Slug(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Slug value cannot be empty");
        }
        this.value = normalize(value);
    }

    private String normalize(String input) {
        // Normalizes input by removing accents and invalid characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Invalid slug after normalization");
        }

        return normalized;
    }

    public static Slug of(String value) {
        return new Slug(value);
    }
}
