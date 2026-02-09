package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.post.PostCreateDTO;
import com.joseguillard.my_blog.dto.post.PostDTO;
import com.joseguillard.my_blog.dto.post.PostSummaryDTO;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.model.Author;
import com.joseguillard.my_blog.model.Category;
import com.joseguillard.my_blog.model.Post;
import com.joseguillard.my_blog.model.enums.PostStatus;
import com.joseguillard.my_blog.model.vo.Slug;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.CategoryRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Returns published Posts (paged)
     */
    public Page<Post> getPublishedPosts(Pageable pageable) {
        return postRepository.findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                PostStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );
    }

    /**
     * Search Post by Slug (increments view count)
     */
    @Transactional
    public Post findBySlugAndIncrementViews(String slug) {
        Post post = postRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));

        // Increment view count for published
        if (post.isPublished()) {
            post.incrementViewCount();
            postRepository.save(post);
        }
        return post;
    }

    /**
     * Search Post by Slug without increment views (for admin)
     */
    public Post findBySlug(String slug) {
        return postRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));
    }

    /**
     * Returns Posts from a specific year
     */
    public List<Post> getPostByYear(int year) {
        return postRepository.findPublishedPostByYear(year);
    }

    /**
     * Returns Posts from a Category
     */
    public Page<PostSummaryDTO> getPostByCategory(String categorySlug, Pageable pageable) {
        Page<Post> posts = postRepository.findPublishedPostByCategorySlug(Slug.of(categorySlug), pageable);
        return posts.map(PostSummaryDTO::fromEntity);
    }

    /**
     * Returns Posts from an Author
     */
    public Page<PostSummaryDTO> getPostByAuthor(String authorSlug, Pageable pageable) {
        Author author = authorRepository.findBySlug(Slug.of(authorSlug))
                .orElseThrow(() -> ResourceNotFoundException.authorNotFound(authorSlug));

        Page<Post> posts = postRepository.findByAuthorAndStatus(
                author,
                PostStatus.PUBLISHED,
                pageable
        );
        return posts.map(PostSummaryDTO::fromEntity);
    }

    /**
     * Search Posts (simple full text search)
     */
    public Page<PostSummaryDTO> searchPosts(String query, Pageable pageable) {
        Page<Post> posts = postRepository.searchPublishedPosts(query, pageable);
        return posts.map(PostSummaryDTO::fromEntity);
    }

    /**
     * Return years with published Posts
     */
    public List<Integer> getYearsWithPosts() {
        return postRepository.findDistinctYearsWithPublishedPosts();
    }

    /**
     * Creates a Post with categories; throws if author/category missing
     */
    @Transactional
    public PostDTO createPost(PostCreateDTO dto, Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Set<Category> categories = new HashSet<>();
        if (dto.getCategoryIds() != null) {
            // Maps category IDs to entities; throws if not found
            categories = dto.getCategoryIds().stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found"))
                    ).collect(Collectors.toSet());
        }

        // Builds post with title, content, excerpt, and image
        Post post = Post.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug() != null && !dto.getSlug().isBlank()
                        ? Slug.of(dto.getSlug())
                        : null)
                .content(dto.getContent())
                .excerpt(dto.getExcerpt())
                .featuredImage(dto.getFeaturedImage())
                .status(dto.getStatus())
                .author(author)
                .categories(categories)
                .metaDescription(dto.getMetaDescription())
                .metaKeywords(dto.getMetaKeywords())
                .build();

        Post createdPost = postRepository.save(post);
        return PostDTO.fromEntity(createdPost);
    }

    /**
     * Updates post content; persists changes transactionally
     */
    @Transactional
    public PostDTO updatePost(Long id, PostCreateDTO dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Updates slug if provided and not blank
        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            post.setSlug(Slug.of(dto.getSlug()));
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setExcerpt(dto.getExcerpt());
        post.setFeaturedImage(dto.getFeaturedImage());
        post.setStatus(dto.getStatus());
        post.setMetaDescription(dto.getMetaDescription());
        post.setMetaKeywords(dto.getMetaKeywords());

        if (dto.getCategoryIds() != null) {
            // Maps category IDs to a category set
            Set<Category> categories = dto.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found")))
                    .collect(Collectors.toSet());
            post.setCategories(categories);
        }

        Post updatedPost = postRepository.save(post);
        return PostDTO.fromEntity(updatedPost);
    }

    /**
     * Publish a Post
     */
    @Transactional
    public void publishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.publish();
        postRepository.save(post);
    }

    /**
     * Unpublish a Post
     */
    @Transactional
    public void unpublishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.unpublish();
        postRepository.save(post);
    }

    /**
     * Delete a Post
     */
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post does not exists");
        }
        postRepository.deleteById(id);
    }
}
