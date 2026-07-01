package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.CategoryMapper;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.repository.CategoryRepository;
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
public class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setup() {
        category = Category.builder()
                .id(1L)
                .name("Java")
                .build();
    }

    @Test
    @DisplayName("Should return category when found by name")
    void shouldReturnCategoryWhenFoundByName() {
        // Arrange
        when(categoryRepository.findByName("Java")).thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.findByName("Java");

        // Assert
        assertThat(result).isEqualTo(category);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category name does not exist")
    void shouldThrowResourceNotFoundExceptionWhenCategoryNameDoesNotExist() {
        // Arrange
        when(categoryRepository.findByName("Javascript")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.findByName("Javascript"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }
}
