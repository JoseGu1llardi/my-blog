package com.joseguillard.my_blog.service;

import com.joseguillard.my_blog.dto.request.auth.LoginRequest;
import com.joseguillard.my_blog.dto.response.AuthResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.exception.ResourceNotFoundException;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthorRepository authorRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username '{}'", request.getUsername());

        // Authenticate - throws if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Load the author from the database
        Author author = authorRepository.findByUserName(request.getUsername())
                .orElseThrow(() ->
                        ResourceNotFoundException.authorNotFound(request.getUsername()));

        // Generate token
        String token = jwtService.generateToken(request.getUsername());

        log.info("Login successful for username '{}'", author.getUsername());

        // Return token + basic user info
        return AuthResponse.builder()
                .token(token)
                .username(author.getUsername())
                .fullName(author.getFullName())
                .build();
    }
}
