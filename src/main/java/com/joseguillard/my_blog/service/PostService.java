package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.PostMapper;
import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.request.post.PostUpdateRequest;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.exception.*;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.Post;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.CategoryRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import com.joseguillard.my_blog.security.ViewRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostMapper postMapper;
    private final ViewRateLimiter viewRateLimiter;

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Returns published Posts (paged)
     */
    public Page<PostSummaryResponse> getPublishedPosts(Pageable pageable) {
        Page<Post> posts = postRepository
                .findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                        PostStatus.PUBLISHED,
                        LocalDateTime.now(),
                        pageable);

        return posts.map(postMapper::toSummaryResponse);
    }

    /**
     * Search Post by Slug (increments view count)
     */
    @Transactional
    public PostResponse findBySlugAndIncrementViews(String slug, String ipAddress) {
        Post post = postRepository.findBySlugAndStatus(Slug.of(slug), PostStatus.PUBLISHED)
                .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));

        // Increment view count for published
        if (post.isPublished() && viewRateLimiter.shouldIncrementView(ipAddress, slug)) {
                postRepository.incrementViewCount(post.getId());
                log.debug("View count incremented for slug '{}' from IP {}", slug, ipAddress);

            // Reload to get updated viewsCount from DB
            post = postRepository.findBySlugAndStatus(Slug.of(slug), PostStatus.PUBLISHED)
                    .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));
        }

        return postMapper.toResponse(post);
    }

    /**
     * Search Post by Slug without increment views (for admin)
     */
    public PostResponse findBySlug(String slug) {
        Post response =  postRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));

        return postMapper.toResponse(response);
    }

    /**
     * Returns Posts from a specific year
     */
    public List<PostSummaryResponse> getPostByYear(int year) {
        List<Post> posts = postRepository.findPublishedPostByYear(year);

        return posts.stream()
                .map(postMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns Posts from a Category
     */
    public Page<PostSummaryResponse> getPostsByCategory(String categorySlug, Pageable pageable) {
        Page<Post> posts = postRepository.findPublishedPostByCategorySlug(Slug.of(categorySlug), pageable);
        return posts.map(postMapper::toSummaryResponse);
    }

    /**
     * Returns Posts from an Author
     */
    public Page<PostSummaryResponse> getPostByAuthor(String authorSlug, Pageable pageable) {
        Author author = authorRepository.findBySlug(Slug.of(authorSlug))
                .orElseThrow(() -> ResourceNotFoundException.authorNotFound(authorSlug));

        Page<Post> posts =  postRepository.findByAuthorAndStatus(
                author,
                PostStatus.PUBLISHED,
                pageable
        );

        return posts.map(postMapper::toSummaryResponse);
    }

    /**
     * Search Posts (simple full text search)
     */
    public Page<PostSummaryResponse> searchPosts(String query, Pageable pageable) {
        Page<Post> posts =  postRepository.searchPublishedPosts(query, pageable);

        return posts.map(postMapper::toSummaryResponse);
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
    public PostResponse createPost(PostCreateRequest request, Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Set<Category> categories = Collections.emptySet();

        // Finds categories; throws if any are missing
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categoryList =
                    categoryRepository.findAllById(request.getCategoryIds());

            if (categoryList.size() != request.getCategoryIds().size())
                throw new ResourceNotFoundException("Some categories were not found");

            categories = new HashSet<>(categoryList);
        }

        // Generate slug from title if not provided
        String slugValue = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : request.getTitle();

        Slug slug = Slug.of(slugValue);

        Post post = postMapper.toEntity(request, author, categories);
        post.setSlug(slug); // Ensure the generated slug is set

        log.info("Creating post with slug '{}' for authorId {}", slug.getValue(), authorId);

        try {
            Post createdPost = postRepository.save(post);
            log.info("Post created successfully with slug '{}'", slug.getValue());
            return postMapper.toResponse(createdPost);
        } catch (DataIntegrityViolationException e) {
            log.warn("Slug conflict on post creation: {}", slug.getValue());
            throw new DuplicatedResourceException("Post already exists: " + slug.getValue());
        }
    }

    /**
     * Updates post-content; persists changes transactionally
     */
    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request, Long requesterId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        validateOwnership(post, requesterId);

        log.info("Updating post id {}", id);

        postMapper.toUpdate(post, request);

        if (request.getCategoryIds() != null) {
            // Maps category IDs to a category set
            Set<Category> categories = new HashSet<>(
                    categoryRepository.findAllById(request.getCategoryIds())
            );
            post.setCategories(categories);
        }

        log.info("Post id {} updated successfully", id);
        return postMapper.toResponse(post);
    }

    /**
     * Publish a Post
     */
    @Transactional
    public void publishPost(Long id, Long requesterId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        validateOwnership(post, requesterId);

        log.info("Publishing post id {}", id);

        if (post.isPublished()) {
            log.warn("Attempted to publish already published post id {}", id);
            throw new PostStateConflictException("Post is already published");
        }

        post.publish();
        log.info("Post id {} published successfully", id);
    }

    /**
     * Unpublish a Post
     */
    @Transactional
    public void unpublishPost(Long id, Long requesterId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        validateOwnership(post, requesterId);

        log.info("Unpublishing post id {}", id);

        if (!post.isPublished()) {
            log.warn("Attempted to unpublish non-published post id {}", id);
            throw new PostStateConflictException("Post is not published");
        }

        post.unpublish();
        log.info("Post id {} unpublished successfully", id);
    }

    /**
     * Delete a Post
     */
    @Transactional
    public void deletePost(Long id, Long requesterId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        validateOwnership(post, requesterId);

        log.info("Deleting post id {}", id);

        post.setDeleted(true);
        post.setDeletedAt(LocalDateTime.now());
        post.unpublish();

        postRepository.save(post);

        log.info("Post id {} deleted successfully", id);
    }

    private void validateOwnership(Post post, Long requesterId) {
        if (!post.getAuthor().getId().equals(requesterId)) {
            log.warn("Ownership violation: author {} attempted to modify post owned by {}",
                    requesterId, post.getAuthor().getId());
            throw new PostOwnershipException();
        }
    }
    }
