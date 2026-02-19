package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.PostMapper;
import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.request.post.PostUpdateRequest;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.exception.BusinessException;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.Post;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.CategoryRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostMapper postMapper;

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
    public PostResponse findBySlugAndIncrementViews(String slug) {
        Post post = postRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));

        // Increment view count for published
        if (post.isPublished()) {
            post.incrementViewCount();
        }

        return postMapper.toResponse(post);
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

        if (postRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug already exists");
        }

        Post post = postMapper.toEntity(request, author, categories);
        post.setSlug(slug); // Ensure the generated slug is set
        Post createdPost = postRepository.save(post);

        return postMapper.toResponse(createdPost);
    }

    /**
     * Updates post content; persists changes transactionally
     */
    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        postMapper.toUpdate(post, request);

        if (request.getCategoryIds() != null) {
            // Maps category IDs to a category set
            Set<Category> categories = new HashSet<>(
                    categoryRepository.findAllById(request.getCategoryIds())
            );
            post.setCategories(categories);
        }

        return postMapper.toResponse(post);
    }

    /**
     * Publish a Post
     */
    @Transactional
    public void publishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (post.isPublished()) {
            throw new BusinessException("Post is already published");
        }

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
