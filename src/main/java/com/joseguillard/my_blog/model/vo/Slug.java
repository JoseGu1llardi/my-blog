package com.joseguillard.my_blog.model.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.text.Normalizer;

@Embeddable
public class Slug implements Serializable {
    private String value;

    protected Slug() {} // JPA requer construtor vazio

    private Slug(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Slug não pode ser vazio");
        }
        this.value = normalize(value);
    }

    private String normalize(String input) {
        // Normalizes input by removing diacritics and invalid characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Slug inválido após normalização");
        }

        return normalized;
    }
}
