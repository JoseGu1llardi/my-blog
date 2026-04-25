package com.joseguillard.my_blog.controller;

import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.request.post.PostUpdateRequest;
import com.joseguillard.my_blog.dto.response.ApiResponse;
import com.joseguillard.my_blog.dto.response.PageResponse;
import com.joseguillard.my_blog.dto.response.post.PostResponse;
import com.joseguillard.my_blog.dto.response.post.PostSummaryResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.service.PostService;
import com.joseguillard.my_blog.utils.IpExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    @GetMapping("/my-posts/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal Author author
    ) {
        PostResponse response = postService.getPostById(id, author.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getMyPosts(
            @AuthenticationPrincipal Author author,
            @PageableDefault() Pageable pageable
    ) {
        Page<PostResponse> responses = postService.findMyPosts(author.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getAllPublishedPosts(
            @PageableDefault() Pageable pageable
    ) {
        Page<PostSummaryResponse> pageResponse = postService.getPublishedPosts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(PageResponse.of(pageResponse))
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostBySlug(
            @PathVariable String slug,
            HttpServletRequest request) {
        String ipAddress = IpExtractor.extractClientIp(request);
        PostResponse response = postService.findBySlugAndIncrementViews(slug, ipAddress);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getPostByYear(
            @PathVariable @Min(2026) @Max(2100) int year) {
        List<PostSummaryResponse> responseList = postService.getPostByYear(year);

        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getPostsByCategory(
            @PathVariable String slug,
            @PageableDefault() Pageable pageable
    ) {
        Page<PostSummaryResponse> responses = postService.getPostsByCategory(slug, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    @GetMapping("/author/{slug}")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getPostsByAuthor(
            @PathVariable String slug,
            @PageableDefault(sort = "publishedAt") Pageable pageable
    ) {
        Page<PostSummaryResponse> responses = postService.getPostByAuthor(slug, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> searchPosts(
            @RequestParam @Size(min = 1, max = 200,
                    message = "Query must be between 1 and 200 characters") String query,
            @PageableDefault() Pageable pageable
    ) {
        Page<PostSummaryResponse> responses = postService.searchPosts(query, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getYearsWithPosts() {
        List<Integer> years = postService.getYearsWithPosts();

        return ResponseEntity.ok(ApiResponse.success(years));
    }

    /**
     * Creates post; returns location and success status
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal Author author
            ) {
        PostResponse post = postService.createPost(request, author.getId());

        URI location = URI.create("/api/v1/posts/" + post.getSlug());

        return ResponseEntity
                .created(location)
                .body(ApiResponse.success("Post created successfully", post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal Author author
    ) {
        PostResponse response = postService.updatePost(id, request, author.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal Author author
    ) {
        postService.publishPost(id, author.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal Author author
            ) {
        postService.unpublishPost(id, author.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal Author author
            ) {
        postService.deletePost(id, author.getId());

        return ResponseEntity.noContent().build();
    }
}
