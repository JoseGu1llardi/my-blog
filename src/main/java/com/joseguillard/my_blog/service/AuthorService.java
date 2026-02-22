package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.AuthorMapper;
import com.joseguillard.my_blog.dto.response.author.AuthorResponse;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
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
    private final AuthorMapper authorMapper;

    public AuthorResponse findBySlug(String slug) {
        Author author = authorRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.authorNotFound(slug));

        return authorMapper.toResponse(author);
    }

    public List<AuthorSummaryResponse> findAllActive() {
        List<Author> authors = authorRepository.findByActiveTrue();

        return authors.stream().map(authorMapper::toSummaryResponse).toList();
    }

    public List<AuthorSummaryResponse> findAuthorWithPosts() {
        List<Author> authors =  authorRepository.findAuthorsWithPublishedPosts();

        return authors.stream().map(authorMapper::toSummaryResponse).toList();
    }

    public AuthorResponse findAuthorByUsername(String username) {
        Author author = authorRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        return authorMapper.toResponse(author);
    }

    public AuthorResponse findAuthorByEmail(String email) {
        Author author = authorRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        return authorMapper.toResponse(author);
    }

    public boolean isUsernameExists(String username) {
        return authorRepository.existsByUserName(username);
    }

    public boolean isEmailExists(String email) {
        return authorRepository.existsByEmail(Email.of(email));
    }
}
