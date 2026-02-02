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

    public static CategoryDTO fromEntity(Category category) {
        return Category.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug().getValue())
                .
    }
}
