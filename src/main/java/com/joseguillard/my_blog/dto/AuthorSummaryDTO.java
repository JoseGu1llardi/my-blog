package com.joseguillard.my_blog.dto;

import com.joseguillard.my_blog.model.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorSummaryDTO {

    private Long id;
    private String fullName;
    private String slug;
    private String avatarUrl;

    public static AuthorSummaryDTO fromEntity(Author author) {
        // Maps author fields to DTO properties
        return AuthorSummaryDTO.builder()
                .id(author.getId())
                .fullName(author.getFullName())
                .slug(author.getSlug().getValue())
                .avatarUrl(author.getAvatarUrl())
                .build();
    }
}
