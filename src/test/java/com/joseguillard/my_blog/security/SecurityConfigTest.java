package com.joseguillard.my_blog.security;

import com.joseguillard.my_blog.config.SecurityConfig;
import com.joseguillard.my_blog.controller.AuthController;
import com.joseguillard.my_blog.controller.AuthorController;
import com.joseguillard.my_blog.controller.CategoryController;
import com.joseguillard.my_blog.controller.PostController;
import com.joseguillard.my_blog.dto.response.AuthResponse;
import com.joseguillard.my_blog.service.AuthService;
import com.joseguillard.my_blog.service.AuthorService;
import com.joseguillard.my_blog.service.CategoryService;
import com.joseguillard.my_blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PostController.class,
        AuthController.class,
        AuthorController.class,
        CategoryController.class
})
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtAuthenticationFilter jwtAuthFilter;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private PostService postService;
    @MockBean private AuthService authService;
    @MockBean private AuthorService authorService;
    @MockBean private CategoryService categoryService;
    @MockBean private LoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            invocation.getArgument(2, FilterChain.class).doFilter(
                    invocation.getArgument(0),
                    invocation.getArgument(1)
            );
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        when(postService.getPublishedPosts(any())).thenReturn(Page.empty());
        when(categoryService.findAll()).thenReturn(Collections.emptyList());
        when(authorService.findAllActive()).thenReturn(Collections.emptyList());
        when(authService.login(any(), any())).thenReturn(
                AuthResponse.builder().token("fake-token").username("test").build()
        );
    }

    @Test
    @DisplayName("Should allow public access to get posts")
    void shouldAllowPublicAccessToGetPosts() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow public access to get categories")
    void shouldAllowPublicAccessToGetCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow public access to get authors")
    void shouldAllowPublicAccessToGetAuthors() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow public access to login")
    void shouldAllowPublicAccessToLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isOk());
    }

    // Protected routes
    @Test
    @DisplayName("Should return 401 when accessing my-posts without token")
    void shouldReturn401WhenAccessingMyPostsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/posts/my-posts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when creating post without token")
    void shouldReturn401WhenCreatingPostWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType("application/json")
                        .content("{\"title\":\"test\",\"content\":\"test\"}"))
                .andExpect(status().isUnauthorized());
    }
}
