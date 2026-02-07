package com.joseguillard.my_blog.dto.response.post;

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
public class PostSummaryResponse {

    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String featuredImage;
    private LocalDateTime publishedAt;
    private Integer readingTimeMinutes;

    private String authorName;
    private String authorSlug;
    private String authorAvatar;

    public String getFormattedPublishedDate() {
        if (publishedAt == null) return "";
        return publishedAt.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy"));
    }
}
