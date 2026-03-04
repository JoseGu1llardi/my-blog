package com.joseguillard.my_blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {

    private final LocalDateTime FIXED_DATE =
            LocalDateTime.of(2026, Month.MARCH, 5, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    private PostResponse  postResponse;
    private PostSummaryResponse postSummaryResponse;

    @BeforeEach
    void setup() {
        AuthorSummaryResponse author = AuthorSummaryResponse.builder()
                .id(1L)
                .fullName("Jose Guillard")
                .slug("joseguillard")
                .build();

        postResponse = PostResponse.builder()
                .id(1L)
                .title("Post Title")
                .content("Content")
                .author(author)
                .slug("post-title")
                .status(PostStatus.PUBLISHED)
                .publishedAt(FIXED_DATE)
                .build();

        postSummaryResponse = PostSummaryResponse.builder()
                .id(1L)
                .title("Post Title")
                .authorName("Jose Guillard")
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

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return a post")
    void shouldReturnPostBySlug() throws Exception {
        when(postService.findBySlugAndIncrementViews("post-title"))
                .thenReturn(postResponse);

        mockMvc.perform(get("/api/v1/posts/{slug}", "post-title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("post-title"))
                .andExpect(jsonPath("$.data.title").value("Post Title"));

        verify(postService).findBySlugAndIncrementViews("post-title");
    }

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return 404 when post not found")
    void shouldReturn404WhenPostNotFound() throws Exception {
        when(postService.findBySlugAndIncrementViews("post-does-not-exist"))
                .thenThrow(ResourceNotFoundException.postNotFound("post-does-not-exist"));

        mockMvc.perform(get("/api/v1/posts/{slug}",  "post-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        verify(postService).findBySlugAndIncrementViews("post-does-not-exist");
    }

    @Test
    @DisplayName("POST /api/v1/posts?authorId= should create a post")
    void  shouldCreatePost() throws Exception {
        // Arrange
        PostCreateRequest request = PostCreateRequest.builder()
                .title("Post Title")
                .content("Content")
                .status(PostStatus.DRAFT)
                .build();

        when(postService.createPost(request, 1L)).thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                .param("authorId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post created successfully"));
    }
}