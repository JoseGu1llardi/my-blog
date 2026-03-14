package com.joseguillard.my_blog.dto.mapper;

import com.joseguillard.my_blog.dto.request.CategoryCreateRequest;
import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final CategoryRepository categoryRepository;

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;

        // Maps category fields to response fields
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug().getValue())
                .description(category.getDescription())
                .icon(category.getIcon())
                .postCount(categoryRepository.countPostsByCategory(category))
                .build();
    }

    /**
     * Converts create request to a category entity
     */
    public Category toEntity(CategoryCreateRequest category) {
        if (category == null) return null;

        // Builds category with slug or null if blank
        return Category.builder()
                .name(category.getName())
                .slug(category.getSlug() != null && !category.getSlug().isBlank()
                        ? Slug.of(category.getSlug())
                        : null)
                .description(category.getDescription())
                .icon(category.getIcon())
                .build();
    }
}
