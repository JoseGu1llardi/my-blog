package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.PostMapper;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.Post;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.CategoryRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostService postService;

    private Author author;
    private Category category;
    private Post post;

    @BeforeEach
    public void setUp() {
        author = Author.builder()
                .userName("Grillard 10")
                .email(Email.of("junior11_junior@hotmail.com"))
                .fullName("Jose Guillard")
                .role(UserRole.AUTHOR)
                .build();

        category = Category.builder()
                .name("Technology")
                .description("This is the description")
                .icon("⚙\uFE0F")
                .build();

        post = Post.builder()
                .title("Post title")
                .content("Post content")
                .author(author)
                .status(PostStatus.PUBLISHED)
                .viewsCount(0)
                .publishedAt(LocalDateTime.of(
                        2026, Month.FEBRUARY, 2, 20, 10))
                .build();
    }

    @Test
    @DisplayName("Should return paginated published posts")
    void shouldReturnPublishedPosts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post));

        when(postRepository.findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                eq(PostStatus.PUBLISHED),
                any(LocalDateTime.class),
                eq(pageable)
        )).thenReturn(page);

        PostSummaryResponse summaryResponse = PostSummaryResponse.builder()
                .title(post.getTitle())
                .build();

        when(postMapper.toSummaryResponse(any(Post.class))).thenReturn(summaryResponse);

        // Act
        Page<PostSummaryResponse> result = postService.getPublishedPosts(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(post.getTitle());

        verify(postRepository, times(1)).findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                any(), any(), any()
        );
        verify(postMapper, times(1)).toSummaryResponse(any(Post.class));
    }

    @Test
    @DisplayName("Should find post by slug and increment views")
    void shouldFindBySlugAndIncrementViews() {
        // Arrange
        when(postRepository.findBySlug(Slug.of("Post title")))
                .thenReturn(Optional.of(post));

        PostResponse postResponse = PostResponse.builder()
                .title("Post title")
                .build();

        when(postMapper.toResponse(any(Post.class))).thenReturn(postResponse);

        // Act
        PostResponse result = postService.findBySlugAndIncrementViews("Post title");

        // Assert
        assertThat(result).isNotNull();

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper, times(1)).toResponse(captor.capture());
        assertThat(captor.getValue().getViewsCount()).isEqualTo(1);

        verify(postRepository, times(1)).findBySlug(Slug.of("Post title"));
    }
}
