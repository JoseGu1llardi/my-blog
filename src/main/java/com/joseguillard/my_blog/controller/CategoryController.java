package com.joseguillard.my_blog.controller;

import com.joseguillard.my_blog.dto.mapper.CategoryMapper;
import com.joseguillard.my_blog.dto.request.CategoryCreateRequest;
import com.joseguillard.my_blog.dto.response.ApiResponse;
import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAllCategories() {
        List<CategoryResponse> responses = categoryService.findAll();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Creates category; returns location and created category
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request
            ) {
        CategoryResponse newCategory = categoryService.createCategory(request);

        URI location = URI.create("/api/v1/categories/" + newCategory.getId());

        return ResponseEntity
                .created(location)
                .body(ApiResponse.success(newCategory));
    }
}
