package com.joseguillard.my_blog.dto;

import com.joseguillard.my_blog.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private int postCount;

    /**
     * Maps category entity to data transfer object
     */
    public static CategoryDTO fromEntity(Category category) {
        // Maps entity fields to a data transfer object
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug().getValue())
                .description(category.getDescription())
                .icon(category.getIcon())
                .postCount(category.getPostCount())
                .build();
    }
}
