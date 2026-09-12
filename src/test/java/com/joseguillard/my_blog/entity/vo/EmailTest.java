package com.joseguillard.my_blog.entity.vo;

import com.joseguillard.my_blog.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EmailTest {

    @Test
    @DisplayName("Should create e-mail when address is valid")
    void shouldCreateEmailWhenAddressIsValid() {
        Email email = Email.of("jwribeiro.dev@gmail.com");
        assertThat(email.address()).isEqualTo("jwribeiro.dev@gmail.com");
    }

    @Test
    @DisplayName("Should normalize address to lowercase and trim whitespace")
    void shouldNormalizeAddressToLowercaseAndTrim() {
        Email email = Email.of(" JWRIBEIRO.DEV@GMAIL.COM ");
        assertThat(email.address()).isEqualTo("jwribeiro.dev@gmail.com");
    }

    @Test
    @DisplayName("Should return the user part of the address")
    void shouldReturnUserPartOfAddress() {
        Email email = Email.of("jwribeiro.dev@gmail.com");
        assertThat(email.getUser()).isEqualTo("jwribeiro.dev");
    }

    @Test
    @DisplayName("Should return the domain part of the address")
    void shouldReturnDomainPartOfAddress() {
        Email email = Email.of("jwribeiro.dev@gmail.com");
        assertThat(email.getDomain()).isEqualTo("gmail.com");
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when address is null")
    void shouldThrowInvalidEmailExceptionWhenAddressIsNull() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage("E-mail address cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when address is empty")
    void shouldThrowInvalidEmailExceptionWhenAddressIsEmpty() {
        assertThatThrownBy(() -> Email.of(""))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage("E-mail address cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when address format is invalid")
    void shouldThrowIllegalArgumentExceptionWhenAddressFormatIsInvalid() {
        assertThatThrownBy(() -> Email.of("jwribeiro.devgmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid e-mail: jwribeiro.devgmail.com");
    }
}
