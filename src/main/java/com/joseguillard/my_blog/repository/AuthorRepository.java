package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.vo.Slug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findBySlug(Slug slug);

    Optional<Author> findByUserName(String userName);

    List<Author> findByActiveTrue();

    @Query("SELECT DISTINCT a FROM Author a JOIN a.posts p " +
    "WHERE p.status = :status AND a.active = true")
    List<Author> findAuthorsWithPublishedPosts(@Param("status") PostStatus status);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :id AND p.status = :status")
    long countPublishedByAuthorId(@Param("id")  Long id, @Param("status") PostStatus status);
}
