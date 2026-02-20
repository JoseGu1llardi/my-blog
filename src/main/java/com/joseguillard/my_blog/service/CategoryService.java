package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.mapper.CategoryMapper;
import com.joseguillard.my_blog.dto.request.CategoryCreateRequest;
import com.joseguillard.my_blog.dto.response.category.CategoryResponse;
import com.joseguillard.my_blog.exception.BusinessException;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.entity.Category;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public List<CategoryResponse> findAll() {
        List<Category> categories =  categoryRepository.findAllByOrderByNameAsc();

        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    public List<Category> findCategoryWithPosts() {
        return categoryRepository.findCategoriesWithPosts();
    }

    public Category findCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(Slug.of(slug))
                .orElseThrow(() -> ResourceNotFoundException.categoryNotFound(slug));
    }

    public boolean isCategoryExistsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category with name " + request.getName() + " already exists");
        }

        Category category = categoryMapper.toEntity(request);
        Category categorySaved = categoryRepository.save(category);

        return categoryMapper.toResponse(categorySaved);
    }
}
