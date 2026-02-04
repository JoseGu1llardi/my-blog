package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.post.PostSummaryDTO;
import com.joseguillard.my_blog.model.Post;
import com.joseguillard.my_blog.model.enums.PostStatus;
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

    public Page<PostSummaryDTO> getPublishedPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
                PostStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );
        return posts.map(PostSummaryDTO::fromEntity);
    }
}
