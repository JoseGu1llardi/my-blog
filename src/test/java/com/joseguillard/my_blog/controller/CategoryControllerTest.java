package com.joseguillard.my_blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.security.JwtService;
import com.joseguillard.my_blog.security.UserDetailsServiceImpl;
import com.joseguillard.my_blog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Only the web layer (controllers, filters, serialization)
@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
// Remove security filters from tests request
// Allows testing endpoints without needing JWT token
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    // Injects the MockMvc configured by @WebMvcTest
    // Simulates HTTP requests without real server
    @Autowired
    private MockMvc mockMvc;

    // Serializes/Deserialize Java objects to JSON and vice versa
    // Used to convert request body into JSON String
    @Autowired
    private ObjectMapper objectMapper;

    // Register a mock in the Spring context
    // Different from Mockito's @Mock - this is injected by the Spring into the controller
    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private CategoryResponse categoryResponse;

    @BeforeEach
    public void setup() {
        categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Java")
                .slug("java")
                .build();
    }

    @Test
    @DisplayName("GET api/v1/categories/{slug} should return a category")
    void shouldReturnCategoryBySlug() throws Exception {
        // Services returns the category when found by slug
        when(categoryService.findCategoryBySlug(eq("java")))
                .thenReturn(categoryResponse);

        // Perform GET request and asserts successful response status and fields
        mockMvc.perform(get("/api/v1/categories/{slug}", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("java"))
                .andExpect(jsonPath("$.data.name").value("Java"));
    }

    @Test
    @DisplayName("GET api/v1/categories/{slug} should return 404 when category not found")
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        // Arrange
        when( categoryService.findCategoryBySlug(eq("category-does-not-exist")))
                .thenThrow(ResourceNotFoundException.categoryNotFound("category-does-not-exist"));

        // Verifies 404 status and error response for missing category
        mockMvc.perform(get("/api/v1/categories/{slug}", "category-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

    }
}
