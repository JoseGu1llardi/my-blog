package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.Post;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.entity.vo.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Author author;
    private Category category;

    @BeforeEach
    void setup() {
        author = Author.builder()
                .userName("joseguillard")
                .email(Email.of("junior11_junior@hotmail.com"))
                .password("hashed-password")
                .fullName("Jose Wellington")
                .role(UserRole.AUTHOR)
                .active(true)
                .build();
        authorRepository.save(author);

        category = Category.builder()
                .name("Technology")
                .description("Posts bout Technology")
                .icon("\uD83D\uDCBB")
                .build();
        categoryRepository.save(category);
    }

    @Test
    @DisplayName("Should successfully save a post")
    void shouldSavePost() {
        // Arrange
        Post post = Post.builder()
                .title("My First Post")
                .content("This is a test post")
                .excerpt("This is a test excerpt")
                .author(author)
                .status(PostStatus.DRAFT)
                .build();

        // Act
        Post savedPost = postRepository.save(post);

        // Assert
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getSlug()).isNotNull();
        assertThat(savedPost.getSlug().getValue()).isEqualTo("my-first-post");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find a post by slug")
    void shouldFindPostBySlug() {
        // Arrange
        Post post = createPost("Test Post", PostStatus.PUBLISHED);
        postRepository.save(post);

        // Act
        Optional<Post> found =  postRepository.findBySlug(post.getSlug());

        // Assert
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getTitle()).isEqualTo("Test Post");
    }

    @Test
    @DisplayName("Should return empty when slug does not exists")
    void shouldReturnEmptyWhenSlugDoesNotExist() {
        // Act
        Optional<Post> post = postRepository.findBySlug(Slug.of("The Slug does not exist"));

        // Assert
        assertThat(post.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should list published post by sorted date")
    void shouldFindPublishedPostByOrderedByDate() {
        // Arrange
        Post post1 = createPost("Old Post", PostStatus.PUBLISHED);
        Post post2 = createPost("New Post", PostStatus.PUBLISHED);
        Post post3 = createPost("Post Draft", PostStatus.DRAFT);

        postRepository.saveAll(List.of(post1, post2, post3));

        // Act
        Page<Post> posts = postRepository.findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                PostStatus.PUBLISHED,
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(posts.getContent()).hasSize(2);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("New Post");
        assertThat(posts.getContent().get(1).getTitle()).isEqualTo("Old Post");
    }

    @Test
    @DisplayName("Should find a post by year")
    void shouldFindPostByYear() {
        // Arrange
        Post post2025 = createPost("2025 Post", PostStatus.PUBLISHED);
        post2025.setPublishedAt(LocalDateTime.of(2025, 10, 5, 0, 0));

        Post post2026 = createPost("2026 Post", PostStatus.PUBLISHED);
        post2026.setPublishedAt(LocalDateTime.of(2026, 5, 5, 0, 0));

        postRepository.saveAll(List.of(post2025, post2026));

        // Act
        List<Post> posts2025 = postRepository.findPublishedPostByYear(2025, PostStatus.PUBLISHED);

        // Assert
        assertThat(posts2025).hasSize(1);
        assertThat(posts2025.get(0).getTitle()).isEqualTo("2025 Post");
    }

    @Test
    @DisplayName("Should find a post by category")
    void shouldFindPostByCategory() {
        // Arrange
        Post post = createPost("Test Post", PostStatus.PUBLISHED);
        post.setCategories(Set.of(category));
        postRepository.save(post);

        // Act
        Page<Post> posts = postRepository.findPublishedPostByCategorySlug(
                category.getSlug(),
                PageRequest.of(0, 10),
                PostStatus.PUBLISHED
                );

        // Assert
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getCategories()).contains(category);
    }

    @Test
    @DisplayName("Should generate slug automatically")
    void shouldGenerateSlugAutomatically() {
        // Arrange
        Post post = Post.builder()
                .title("My blog will open many doors for me")
                .content("Just do It!")
                .author(author)
                .status(PostStatus.DRAFT)
                .build();

        // Act
        Post saved = postRepository.save(post);

        assertThat(saved.getSlug()).isNotNull();
        assertThat(saved.getSlug().getValue()).isEqualTo("my-blog-will-open-many-doors-for-me");
    }

    // Auxiliary method
    private Post createPost(String title, PostStatus status) {
        return Post.builder()
                .title(title)
                .content("This is a test post")
                .excerpt("This is a test excerpt")
                .author(author)
                .status(status)
                .build();
    }
}
