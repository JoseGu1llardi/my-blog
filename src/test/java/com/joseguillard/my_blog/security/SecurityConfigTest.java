package com.joseguillard.my_blog.security;

import com.joseguillard.my_blog.config.SecurityConfig;
import com.joseguillard.my_blog.controller.AuthController;
import com.joseguillard.my_blog.controller.AuthorController;
import com.joseguillard.my_blog.controller.CategoryController;
import com.joseguillard.my_blog.controller.PostController;
import com.joseguillard.my_blog.service.AuthService;
import com.joseguillard.my_blog.service.AuthorService;
import com.joseguillard.my_blog.service.CategoryService;
import com.joseguillard.my_blog.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

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
}
