package com.joseguillard.my_blog.entity.vo;

import lombok.Getter;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Objects;

@Getter
public class Slug implements Serializable {
    private String value;

    protected Slug() {} // JPA requires empty constructor

    private Slug(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Slug value cannot be empty");
        }
        this.value = normalize(value);
    }

    /**
     * Creates a new instance of {@code Slug} from the given value.
     * The provided value is normalized to generate a valid slug.
     */
    public static Slug of(String value) {
        return new Slug(value);
    }

    public static Slug fromTitle(String title) {
        return new Slug(title);
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

    /**
     * Implements equality check based on the underlying value
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Slug slug = (Slug) o;
        return Objects.equals(value, slug.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
