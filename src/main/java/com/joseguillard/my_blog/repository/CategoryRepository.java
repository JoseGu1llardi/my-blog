package com.joseguillard.my_blog.repository;

import com.joseguillard.my_blog.model.Category;
import com.joseguillard.my_blog.model.vo.Slug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(Slug slug);

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE SIZE(c.posts) > 0 ORDER BY c.name")
    List<Category> findCategoriesWithPosts();

    List<Category> findAllByOrderByNameAsc();
}
