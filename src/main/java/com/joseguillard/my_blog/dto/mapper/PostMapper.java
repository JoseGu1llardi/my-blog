package com.joseguillard.my_blog.dto.mapper;

import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.entity.Post;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

    private final AuthorMapper authorMapper;
    private final CategoryMapper categoryMapper;

    public PostMapper(AuthorMapper authorMapper, CategoryMapper categoryMapper) {
        this.authorMapper = authorMapper;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Maps post to response, including author and categories
     */
    public PostResponse toResponse(Post post) {
        if (post == null) return null;

        // Maps post ID, slug, and title to response
        return PostResponse.builder()
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
                .author(authorMapper.toSummaryResponse(post.getAuthor()))
                .categories(post.getCategories().stream()
                        .map(categoryMapper::toResponse)
                        .collect(Collectors.toSet()))
                .metaDescription(post.getMetaDescription())
                .metaKeywords(post.getMetaKeywords())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public PostSummaryResponse toSummaryResponse(Post post) {
        if (post == null) return null;

        // Builds summary response with ID, slug, title
        return PostSummaryResponse.builder()
                .id(post.getId())
                .slug(post.getSlug().getValue())
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .featuredImage(post.getFeaturedImage())
                .publishedAt(post.getPublishedAt())
                .readingTimeMinutes(post.getReadingTimeMinutes())
                .authorName(post.getAuthor().getFullName())
                .authorSlug(post.getAuthor().getSlug().getValue())
                .authorAvatar(post.getAuthor().getAvatarUrl())
                .build();
    }
}
