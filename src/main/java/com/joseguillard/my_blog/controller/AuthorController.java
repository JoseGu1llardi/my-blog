package com.joseguillard.my_blog.controller;

import com.joseguillard.my_blog.dto.response.ApiResponse;
import com.joseguillard.my_blog.dto.response.author.AuthorResponse;
import com.joseguillard.my_blog.dto.response.author.AuthorSummaryResponse;
import com.joseguillard.my_blog.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorSummaryResponse>>> getAllAuthors() {
        List<AuthorSummaryResponse> authors = authorService.findAllActive();

        return ResponseEntity.ok(ApiResponse.success(authors));
    }

    @GetMapping("/with-posts")
    public ResponseEntity<ApiResponse<List<AuthorSummaryResponse>>> getAuthorsWithPosts() {
        List<AuthorSummaryResponse> authors = authorService.findAuthorWithPosts();

        return ResponseEntity.ok(ApiResponse.success(authors));
    }
}
