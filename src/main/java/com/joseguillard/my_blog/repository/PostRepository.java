package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.model.Author;
import com.joseguillard.my_blog.model.Post;
import com.joseguillard.my_blog.model.enums.PostStatus;
import com.joseguillard.my_blog.model.vo.Slug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' " +
    "AND YEAR(p.publishedAt) = :year " +
    "ORDER BY p.publishedAt DESC")
    List<Post> findPublishedPostByYear(@Param("year") int year);

    @Query("SELECT p from Post p JOIN p.categories c " +
    "WHERE c.slug = :categorySlug AND p.status = 'PUBLISHED' " +
    "ORDER BY p.publishedAt DESC")
    Page<Post> findPublishedPostByCategorySlug(@Param("categorySlug") Slug categorySlug, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.publishedAt DESC")
    Page<Post> searchPublishedPosts(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT YEAR(p.publishedAt) FROM Post p " +
            "WHERE p.status = 'PUBLISHED' " +
            "ORDER BY YEAR(p.publishedAt) DESC")
    List<Integer> findDistinctYearsWithPublishedPosts();

    long countByAuthorAndStatus(Author author, PostStatus status);
}
