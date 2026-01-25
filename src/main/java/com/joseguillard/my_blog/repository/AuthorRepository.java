package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.model.Author;
import com.joseguillard.my_blog.model.vo.Email;
import com.joseguillard.my_blog.model.vo.Slug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findBySlug(Slug slug);

    Optional<Author> findByUserName(String userName);

    Optional<Author> findByEmail(Email email);
}
