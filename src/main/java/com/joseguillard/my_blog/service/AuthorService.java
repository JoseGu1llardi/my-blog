package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    public Author findBySlug(String slug) {
        return authorRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.authorNotFound(slug));
    }

    public List<Author> findAllActive() {
        return authorRepository.findByActiveTrue();
    }

    public List<Author> findAuthorWithPosts() {
        return authorRepository.findAuthorsWithPublishedPosts();
    }

    public Author findAuthorByUsername(String username) {
        return authorRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
    }

    public Author findAuthorByEmail(String email) {
        return authorRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
    }

    public boolean isUsernameExists(String username) {
        return authorRepository.existsByUserName(username);
    }

    public boolean isEmailExists(String email) {
        return authorRepository.existsByEmail(Email.of(email));
    }
}
