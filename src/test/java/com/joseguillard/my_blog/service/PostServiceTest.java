package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.AuthorMapper;
import com.joseguillard.my_blog.dto.mapper.PostMapper;
import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.Post;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.CategoryRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    private Author author;
    private Category category;
    private Post post;

    @BeforeEach
    public void setUp() {
        author = Author.builder()
                .id(1L)
                .userName("Grillard 10")
                .email(Email.of("junior11_junior@hotmail.com"))
                .fullName("Jose Guillard")
                .role(UserRole.AUTHOR)
                .build();

        category = Category.builder()
                .id(1L)
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
        // Simulates tha the post exists
        when(postRepository.findBySlug(Slug.of("post-title")))
                .thenReturn(Optional.of(post));

        PostResponse postResponse = PostResponse.builder()
                .title("Post title")
                .build();

        // Simulates the mapper
        when(postMapper.toResponse(any(Post.class))).thenReturn(postResponse);

        // Here the real rule is executed
        PostResponse result = postService.findBySlugAndIncrementViews("post-title");

        // Assert
        assertThat(result).isEqualTo(postResponse);

        // Capturing the object sent to the mapper
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).toResponse(captor.capture());
        assertThat(captor.getValue().getViewsCount()).isEqualTo(1);

        verify(postRepository).findBySlug(Slug.of("post-title"));
    }

    @Test
    @DisplayName("Should not increment views when post is not published")
    void shouldNotIncrementViewsWhenPostIsNotPublished() {
        // Arrange
        post.setStatus(PostStatus.DRAFT);
        post.setViewsCount(0);

        when(postRepository.findBySlug(any())).thenReturn(Optional.of(post));
        when(postMapper.toResponse(any(Post.class))).thenReturn(new PostResponse());

        // Act
        postService.findBySlugAndIncrementViews("post-title");

        // Assert
        assertThat(post.getViewsCount()).isEqualTo(0);
        verify(postRepository).findBySlug(Slug.of("post-title"));
        verify(postMapper).toResponse(post);
    }

    @Test
    @DisplayName("Should throw an exception when post does not exists")
    void shouldThrowExceptionWhenPostDoesNotExists() {
        // Arrange
        when(postRepository.findBySlug(Slug.of("does-not-exist")))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.findBySlug("does-not-exist"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("does-not-exist");

        verify(postRepository).findBySlug(Slug.of("does-not-exist"));
    }

    @Test
    @DisplayName("Should create a post successfully and delegate mapping correctly")
    void  shouldCreatePost() {
        // Arrange
        PostCreateRequest request = PostCreateRequest.builder()
                .title("New Post")
                .content("New Post content")
                .status(PostStatus.DRAFT)
                .categoryIds(Set.of(1L))
                .build();

        // Author exists
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        // Categories exists
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));

        // Mapper creates entity
        Post mappedPost = Post.builder()
                .title("New Post")
                .content("New Post content")
                .author(author)
                .status(PostStatus.DRAFT)
                .build();

        when(postMapper.toEntity(request, author, Set.of(category))).thenReturn(mappedPost);

        // Saves return an object itself - simulating persistence
        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Response mapper
        PostResponse expectedResponse = PostResponse.builder()
                .title("New Post")
                .build();

        when(postMapper.toResponse(mappedPost)).thenReturn(expectedResponse);

        // Act
        PostResponse result = postService.createPost(request, 1L);

        // Assert

        // Return is exactly what the mapper returned
        assertThat(result).isSameAs(expectedResponse);

        // Ensures logical order of orchestration
        InOrder inOrder = inOrder(authorRepository, categoryRepository, postMapper, postRepository);

        inOrder.verify(authorRepository).findById(1L);
        inOrder.verify(categoryRepository).findAllById(Set.of(1L));
        inOrder.verify(postMapper).toEntity(request, author, Set.of(category));
        inOrder.verify(postRepository).save(mappedPost);
        inOrder.verify(postMapper).toResponse(mappedPost);
    }

    @Test
    @DisplayName("Should publish a post")
    void shouldPublishPost() {
        // Arrange
        post.setStatus(PostStatus.DRAFT);
        post.setPublishedAt(null);
        post.setViewsCount(0);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Act
        postService.publishPost(1L);

        // Assert
        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getPublishedAt()).isNotNull();

        verify(postRepository).findById(1L);
        verify(postRepository).save(post);
    }

}
