package com.joseguillard.my_blog.dto.author;

import com.joseguillard.my_blog.model.Author;
import com.joseguillard.my_blog.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author's complete DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {

    private Long id;
    private String userName;
    private String email;
    private String fullName;
    private String slug;
    private String bio;
    private String avatarUrl;
    private String website;
    private String github;
    private String x;
    private String linkedIn;
    private UserRole role;
    private boolean active;
    private long postCount;

    public static AuthorDTO fromEntity(Author author) {
        return AuthorDTO.builder()
                .id(author.getId())
                .userName(author.getUserName())
                .email(author.getEmail().address())
                .fullName(author.getFullName())
                .slug(author.getSlug().getValue())
                .bio(author.getBio())
                .avatarUrl(author.getAvatarUrl())
                .website(author.getWebsite())
                .github(author.getGithub())
                .x(author.getX())
                .linkedIn(author.getLinkedin())
                .role(author.getRole())
                .active(author.isActive())
                .postCount(author.getPosts().size())
                .build();
    }
}
