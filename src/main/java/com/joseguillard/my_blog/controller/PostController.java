package com.joseguillard.my_blog.controller;

import com.joseguillard.my_blog.dto.mapper.PostMapper;
import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.response.ApiResponse;
import com.joseguillard.my_blog.dto.response.PageResponse;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostSummaryResponse> pageResponse = postService.getPublishedPosts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(PageResponse.of(pageResponse))
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostBySlug(
            @PathVariable String slug) {
        PostResponse response = postService.findBySlugAndIncrementViews(slug);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getPostByYear(
            @PathVariable int year) {
        List<PostSummaryResponse> responseList = postService.getPostByYear(year);

        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getPostsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostSummaryResponse> response = postService.getPostsByCategory(slug, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(response)));
    }

    @GetMapping("/author/{slug}")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getPostsByAuthor(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostSummaryResponse> response = postService.getPostByAuthor(slug, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(response)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @RequestParam Long authorId
            ) {
        PostResponse post = postService.createPost(request, authorId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", post));
    }
}
