package com.joseguillard.my_blog.controller;

import com.joseguillard.my_blog.dto.request.auth.LoginRequest;
import com.joseguillard.my_blog.dto.response.ApiResponse;
import com.joseguillard.my_blog.dto.response.AuthResponse;
import com.joseguillard.my_blog.exception.RateLimitExceededException;
import com.joseguillard.my_blog.security.LoginRateLimiter;
import com.joseguillard.my_blog.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginRateLimiter rateLimiter;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();

        if (!rateLimiter.isAllowed(ipAddress)) {
            throw new RateLimitExceededException("Rate limit exceeded");
        }

        AuthResponse login = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(login));
    }
}
