package com.joseguillard.my_blog.dto;

import com.joseguillard.my_blog.model.Category;
import com.joseguillard.my_blog.model.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

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
}
