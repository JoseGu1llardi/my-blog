package com.joseguillard.my_blog.controller;

import com.joseguillard.my_blog.dto.response.author.AuthorResponse;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.security.JwtService;
import com.joseguillard.my_blog.security.UserDetailsServiceImpl;
import com.joseguillard.my_blog.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(
        controllers = AuthorController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private AuthorResponse authorResponse;
    private AuthorSummaryResponse authorSummaryResponse;

    @BeforeEach
    public void setup() {
        authorResponse = AuthorResponse.builder()
                .username("joseguillard")
                .fullName("Jose Wellington")
                .slug("joseguillard")
                .build();

        authorSummaryResponse = AuthorSummaryResponse.builder()
                .fullName("Jose Wellington")
                .slug("jose-wellington")
                .build();
    }

    @Test
    @DisplayName("GET api/v1/authors/{slug} should return an author")
    void shouldReturnAuthorBySlug() throws Exception {
        when(authorService.findBySlug("joseguillard"))
                .thenReturn(authorResponse);

        mockMvc.perform(get("/api/v1/authors/{slug}", "joseguillard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("joseguillard"));
    }

    @Test
    @DisplayName("GET api/v1/authors/{slug} should return 404 when author not found")
    void shouldReturn404WhenAuthorNotFound() throws Exception {
        // Arrange
        when(authorService.findBySlug("joseguillard"))
                .thenThrow(ResourceNotFoundException.authorNotFound("joseguillard"));

        // Verifies 404 status and error response for missing category
        mockMvc.perform(get("/api/v1/authors/{slug}", "joseguillard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET api/v1/authors should return a list of active authors")
    void shouldReturnAllAuthors() throws Exception {
        // Arrange
        List<AuthorSummaryResponse> authors = List.of(authorSummaryResponse);

        when(authorService.findAllActive()).thenReturn(authors);

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("jose-wellington"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
