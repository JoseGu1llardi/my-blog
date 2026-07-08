package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.AuthorMapper;
import com.joseguillard.my_blog.dto.response.author.AuthorResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock private AuthorRepository authorRepository;
    @Mock private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorService authorService;

    private Author author;

    @BeforeEach
    void setup() {
        author = Author.builder()
                .id(1L)
                .userName("userName")
                .fullName("fullName")
                .tokenVersion(1)
                .build();
    }

    @Test
    @DisplayName("Should increment token version")
    void shouldIncrementTokenVersion() {
        // Arrange
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);

        // Act
        authorService.incrementTokenVersion(1L);

        // Assert
        assertThat(author.getTokenVersion()).isEqualTo(2);
        verify(authorRepository, times(1)).save(author);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when author does not exist")
    void shouldThrowResourceNotFoundExceptionWhenAuthorDoesNotExist() {
        // Arrange
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorService.incrementTokenVersion(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found");
    }

    @Test
    @DisplayName("Should return author when found by slug")
    void shouldReturnAuthorWhenFoundBySlug() {
        // Arrange
        when(authorRepository.findBySlug(Slug.of("userName"))).thenReturn(Optional.of(author));

        AuthorResponse expectedResponse = new AuthorResponse();
        when(authorMapper.toResponse(author)).thenReturn(expectedResponse);

        // Act
        AuthorResponse result = authorService.findBySlug("userName");

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when author not found by slug")
    void shouldThrowResourceNotFoundExceptionWhenAuthorNotFoundBySlug() {
        // Arrange
        when(authorRepository.findBySlug(Slug.of("Username"))).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorService.findBySlug("Username"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author with slug 'Username' not found");
    }
}
