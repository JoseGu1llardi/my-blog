package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.entity.vo.Slug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findBySlug(Slug slug);

    Optional<Author> findByUserName(String userName);

    Optional<Author> findByEmail(Email email);

    boolean existsByUserName(String userName);

    boolean existsByEmail(Email email);

    List<Author> findByActiveTrue();

    @Query("SELECT a FROM Author a WHERE SIZE(a.posts) > 0 AND a.active = true")
    List<Author> findAuthorsWithPublishedPosts();
}
