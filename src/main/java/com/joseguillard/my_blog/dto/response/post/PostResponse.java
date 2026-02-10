package com.joseguillard.my_blog.dto.response.post;

import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String slug;
    private String title;
    private String content;
    private String excerpt;
    private String featuredImage;
    private PostStatus status;
    private LocalDateTime publishedAt;
    private Integer viewCount;
    private Integer readingTimeMinutes;

    private AuthorSummaryResponse author;
    private Set<CategoryResponse> categories;

    private String metaDescription;
    private String metaKeywords;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFormattedPublishedDate() {
        if (publishedAt == null) return "";
        return publishedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public int getPublishedYear() {
        return publishedAt != null ? publishedAt.getYear() : 0;
    }
}
