package com.joseguillard.my_blog.dto.post;

import com.joseguillard.my_blog.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDTO {

    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String featuredImage;
    private LocalDateTime publishedAt;
    private Integer readingTimeMinutes;

    // Author info (only the essential)
    private String authorName;
    private String authorSlug;

    public String getFormattedPublishedDate() {
        if (publishedAt == null) return "";
        return this.publishedAt.format(DateTimeFormatter.ofPattern("dd 'of' MMM, yyyy"));
    }

    public static PostSummaryDTO fromEntity(Post post) {
        return PostSummaryDTO.builder()
                .id(post.getId())
                .slug(post.getSlug().getValue())
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .featuredImage(post.getFeaturedImage())
                .publishedAt(post.getPublishedAt())
                .readingTimeMinutes(post.getReadingTimeMinutes())
                .authorName(post.getAuthor().getFullName())
                .authorSlug(post.getAuthor().getSlug().getValue())
                .build();
    }
}
