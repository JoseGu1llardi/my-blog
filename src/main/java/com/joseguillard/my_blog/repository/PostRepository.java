package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.model.Author;
import com.joseguillard.my_blog.model.Post;
import com.joseguillard.my_blog.model.enums.PostStatus;
import com.joseguillard.my_blog.model.vo.Slug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(Slug slug);

    Page<Post> findByStatusOrderByPublishedAtDesc(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndPublishedAtBeforeOrderByPublishedAtDesc(
            PostStatus status,
            LocalDateTime publishedAt,
            Pageable pageable
    );

    Page<Post> findByAuthorAndStatus(Author author, PostStatus status, Pageable pageable);
}
