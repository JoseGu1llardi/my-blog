package com.joseguillard.my_blog.dto.post;

import com.joseguillard.my_blog.dto.author.AuthorSummaryDTO;
import com.joseguillard.my_blog.dto.CategoryDTO;
import com.joseguillard.my_blog.model.Post;
import com.joseguillard.my_blog.model.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Full DTO of Post for displaying details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

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

    // Author info
    private AuthorSummaryDTO author;

    // Categories
    private Set<CategoryDTO> categories;

    // SEO
    private String metaDescription;
    private String metaKeywords;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods for template
    public String getFormattedPublishedDate() {
        if (publishedAt == null) return "";
        return this.publishedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedPublishedDateTime() {
        if (publishedAt == null) return "";
        return this.publishedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public int getPublishedYear() {
        return this.publishedAt != null ? this.publishedAt.getYear() : 0;
    }

    // Factory method
    public static PostDTO fromEntity(Post post) {
        // Builds DTO with post-ID, slug, and title
        return PostDTO.builder()
                .id(post.getId())
                .slug(post.getSlug().getValue())
                .title(post.getTitle())
                .content(post.getContent())
                .excerpt(post.getExcerpt())
                .featuredImage(post.getFeaturedImage())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .viewCount(post.getViewsCount())
                .readingTimeMinutes(post.getReadingTimeMinutes())
                .author(AuthorSummaryDTO.fromEntity(post.getAuthor()))
                .categories(post.getCategories().stream()
                        .map(CategoryDTO::fromEntity)
                        .collect(Collectors.toSet()))
                .metaDescription(post.getMetaDescription())
                .metaKeywords(post.getMetaKeywords())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
