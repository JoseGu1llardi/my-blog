package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.post.PostDTO;
import com.joseguillard.my_blog.dto.post.PostSummaryDTO;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Returns published posts (paged)
     */
    public Page<PostSummaryDTO> getPublishedPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                PostStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );
        return posts.map(PostSummaryDTO::fromEntity);
    }

    /**
     * Search post by slug (increments view count)
     */
    @Transactional
    public PostDTO findBySlug(String slug) {
        Post post = postRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.postNotFound(slug));

        // Increment view count for published
        if (post.isPublished()) {
            post.incrementViewCount();
            postRepository.save(post);
        }

        return PostDTO.fromEntity(post);
    }
}
