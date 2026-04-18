package com.joseguillard.my_blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.security.JwtService;
import com.joseguillard.my_blog.security.UserDetailsServiceImpl;
import com.joseguillard.my_blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PostController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {

    private final LocalDateTime FIXED_DATE =
            LocalDateTime.of(2026, Month.MARCH, 5, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private PostService postService;

    private PostResponse  postResponse;
    private PostSummaryResponse postSummaryResponse;

    /**
     * Initializes mock responses for author and posts
     */
    @BeforeEach
    void setup() {
        AuthorSummaryResponse author = AuthorSummaryResponse.builder()
                .id(1L)
                .fullName("Jose Guillard")
                .slug("joseguillard")
                .build();

        postSummaryResponse = PostSummaryResponse.builder()
                .id(1L)
                .title("Post Title")
                .author(author)
                .publishedAt(FIXED_DATE)
                .build();

        // Builds full post-response with content and metadata
        postResponse = PostResponse.builder()
                .id(1L)
                .title("Post Title")
                .content("Content")
                .author(author)
                .slug("post-title")
                .status(PostStatus.PUBLISHED)
                .publishedAt(FIXED_DATE)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/posts should return a list of posts")
    void shouldReturnListOfPosts() throws Exception {
        // Arrange
        Page<PostSummaryResponse> page =
                new PageImpl<>(List.of(postSummaryResponse));

        when(postService.getPublishedPosts(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title")
                        .value("Post Title"));

        verify(postService).getPublishedPosts(any(Pageable.class));
    }

    /**
     * Verifies endpoint returns post by slug with incremented views; audits service call
     */
    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return a post")
    void shouldReturnPostBySlug() throws Exception {
        when(postService.findBySlugAndIncrementViews(eq("post-title"), any(String.class)))
                .thenReturn(postResponse);

        // Performs GET request and asserts successful response status and fields
        mockMvc.perform(get("/api/v1/posts/{slug}", "post-title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("post-title"))
                .andExpect(jsonPath("$.data.title").value("Post Title"));

        verify(postService).findBySlugAndIncrementViews(eq("post-title"), any(String.class));
    }

    /**
     * Tests endpoint returns 404 when post not found; verifies service call
     */
    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return 404 when post not found")
    void shouldReturn404WhenPostNotFound() throws Exception {
        when(postService.findBySlugAndIncrementViews(eq("post-does-not-exist"), any(String.class)))
                .thenThrow(ResourceNotFoundException.postNotFound("post-does-not-exist"));

        // Verifies 404 status and error response for missing post
        mockMvc.perform(get("/api/v1/posts/{slug}",  "post-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        verify(postService).findBySlugAndIncrementViews(eq("post-does-not-exist"), any(String.class));
    }

    @Test
    @DisplayName("POST /api/v1/posts should create a post")
    void  shouldCreatePost() throws Exception {
        // Arrange
        PostCreateRequest request = PostCreateRequest.builder()
                .title("Post Title")
                .content("Content")
                .build();

        when(postService.createPost(any(PostCreateRequest.class), any()))
                .thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                // Authenticates request as an active author user
                .with(SecurityMockMvcRequestPostProcessors.user(
                    Author.builder()
                        .id(1L)
                        .userName("guillard")
                        .password("password")
                        .role(UserRole.AUTHOR)
                        .active(true)
                        .build()
                        ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Post Title"))
                .andExpect(jsonPath("$.message").value("Post created successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/posts should return 400 with invalid validation")
    void shouldReturn400WithInvalidData() throws Exception {
        // Arrange - request without title (required field)
        PostCreateRequest request = PostCreateRequest.builder()
                .content("Content")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                .param("authorId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }
}