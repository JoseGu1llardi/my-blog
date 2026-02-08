package com.joseguillard.my_blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name must have a maximum of 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must have a maximum of 1000 characters")
    private String description;

    @Size(max = 50, message = "Icon must have a maximum of 50 characters")
    private String icon;
}
