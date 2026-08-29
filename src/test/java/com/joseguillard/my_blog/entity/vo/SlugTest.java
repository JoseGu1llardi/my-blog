package com.joseguillard.my_blog.entity.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SlugTest {

    @Test
    @DisplayName("Should remove accents")
    void shouldRemoveAccentsAndSpecialCharacters() {
        Slug slug = Slug.of("Programação Java");
        assertThat(slug.getValue()).isEqualTo("programacao-java");
    }

    @Test
    @DisplayName("Should convert uppercase to lowercase")
    void shouldConvertUppercaseToLowercase() {
        Slug slug = Slug.of("Java");
        assertThat(slug.getValue()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should replaces spaces by dashes")
    void shouldReplaceSpacesByDashes() {
        Slug slug = Slug.of("Java and Spring");
        assertThat(slug.getValue()).isEqualTo("java-and-spring");
    }

    @Test
    @DisplayName("Should remove special characters")
    void shouldRemoveSpecialCharacters() {
        Slug slug = Slug.of("PostgreSQL & Docker");
        assertThat(slug.getValue()).isEqualTo("postgresql-docker");
    }

    @Test
    @DisplayName("Should replace multiple dashes")
    void shouldReplaceMultipleDashesByOne() {
        Slug slug = Slug.of("NextJS-----Front---End");
        assertThat(slug.getValue()).isEqualTo("nextjs-front-end");
    }

    @Test
    @DisplayName("Should remove dashes from start and end")
    void shouldRemoveDashesFromStartAndEnd() {
        Slug slug = Slug.of("--Java and Spring--");
        assertThat(slug.getValue()).isEqualTo("java-and-spring");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when value is null")
    void shouldThrowIllegalArgumentExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> Slug.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Slug value cannot be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when value is blank")
    void shouldThrowIllegalArgumentExceptionWhenValueIsBlank() {
        assertThatThrownBy(() -> Slug.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Slug value cannot be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when value becomes empty after normalization")
    void shouldThrowIllegalArgumentExceptionWhenValueBecomesEmptyAfterNormalization() {
        assertThatThrownBy(() -> Slug.of("!!!£££***"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid slug after normalization");
    }
}
