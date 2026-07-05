package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.CategoryMapper;
import com.joseguillard.my_blog.dto.request.CategoryCreateRequest;
import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.exception.DuplicatedResourceException;
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

    @Test
    @DisplayName("Should return category when find by slug")
    void shouldReturnCategoryWhenFoundBySlug() {
        // Arrange
        when(categoryRepository.findBySlug(Slug.of("java"))).thenReturn(Optional.of(category));

        CategoryResponse expectedResponse = new CategoryResponse();
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        // Act
        CategoryResponse result = categoryService.findCategoryBySlug("java");

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found by slug")
    void shouldThrowResourceNotFoundExceptionWhenCategoryNotFoundBySlug() {
        // Arrange
        when(categoryRepository.findBySlug(Slug.of("javascript"))).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.findCategoryBySlug("javascript"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category with slug 'javascript' not found");
    }

    @Test
    @DisplayName("Should throw DuplicatedResourceException when category name already exists")
    void shouldThrowDuplicatedResourceExceptionWhenCategoryNameAlreadyExists() {
        // Arrange
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Java")
                .build();

        when(categoryRepository.existsByName("Java")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicatedResourceException.class)
                .hasMessageContaining("Category with name 'Java' already exists");
        }

        @Test
        @DisplayName("Should create and return category when name is available")
        void shouldCreateAndReturnCategoryWhenNameIsAvailable() {
            // Arrange
            CategoryCreateRequest request = CategoryCreateRequest.builder()
                    .name("Java")
                    .build();

            when(categoryRepository.existsByName("Java")).thenReturn(false);

            // Returns a category from @BeforeEach
            when(categoryMapper.toEntity(request)).thenReturn(category);
            // Save receives this category and returns it saves
            when(categoryRepository.save(category)).thenReturn(category);
            // Mapper maps the category to a response
            CategoryResponse expectedResponse = new CategoryResponse();
            when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

            // Act
            CategoryResponse result = categoryService.createCategory(request);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(categoryRepository, times(1)).save(category);
        }
}
