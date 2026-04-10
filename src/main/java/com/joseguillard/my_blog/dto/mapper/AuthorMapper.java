package com.joseguillard.my_blog.dto.mapper;

import com.joseguillard.my_blog.dto.response.author.AuthorResponse;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorMapper {

    private final AuthorRepository authorRepository;

    public AuthorResponse toResponse(Author author) {
        if (author == null) return null;

        long postCount = authorRepository.countPublishedByAuthorId(author.getId());

        // Maps author ID, username, full name, and slug
        return AuthorResponse.builder()
                .username(author.getUsername())
                .fullName(author.getFullName())
                .slug(author.getSlug().getValue())
                .bio(author.getBio())
                .avatarUrl(author.getAvatarUrl())
                .website(author.getWebsite())
                .github(author.getGithub())
                .x(author.getX())
                .linkedin(author.getLinkedin())
                .postCount(postCount)
                .build();
    }

    public AuthorSummaryResponse toSummaryResponse(Author author) {
        if (author == null) return null;

        // Builds summary response from author's core attributes
        return AuthorSummaryResponse.builder()
                .id(author.getId())
                .fullName(author.getFullName())
                .slug(author.getSlug().getValue())
                .avatarUrl(author.getAvatarUrl())
                .build();
    }
}
