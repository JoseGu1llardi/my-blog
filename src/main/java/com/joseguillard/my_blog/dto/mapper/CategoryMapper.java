package com.joseguillard.my_blog.dto.mapper;

import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;

        // Maps category fields to response fields
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug().getValue())
                .description(category.getDescription())
                .icon(category.getIcon())
                .postCount(category.getPostCount())
                .build();
    }
}
