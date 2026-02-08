package com.joseguillard.my_blog.dto.request.post;

import com.joseguillard.my_blog.model.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must have a maximum of 500 characters")
    private String title;

    private String slug;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 1000, message = "Excerpt must have a maximum of 1000 characters")
    private String excerpt;

    private String featuredImage;

    private PostStatus  status = PostStatus.DRAFT;

    private Set<Long> categoryIds;

    @Size(max = 500, message = "Meta description must have a maximum of 500 characters")
    private String metaDescription;

    @Size(max = 500, message = "Meta keywords must have a maximum of 500 characters")
    private String metaKeywords;
}
