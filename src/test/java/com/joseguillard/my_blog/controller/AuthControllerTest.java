package com.joseguillard.my_blog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joseguillard.my_blog.dto.request.auth.LoginRequest;
import com.joseguillard.my_blog.dto.response.AuthResponse;
import com.joseguillard.my_blog.security.JwtService;
import com.joseguillard.my_blog.security.LoginRateLimiter;
import com.joseguillard.my_blog.security.UserDetailsServiceImpl;
import com.joseguillard.my_blog.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration =  SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private LoginRateLimiter loginRateLimiter;

    private AuthResponse authResponse;

    @BeforeEach
    void setup() {
        authResponse = AuthResponse.builder()
                .token("token")
                .username("username")
                .fullName("fullName")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return token when login is successful")
    void shouldReturnTokenWhenLoginIsSuccessful() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("password")
                .build();

        when(loginRateLimiter.isAllowed(any())).thenReturn(true);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token"))
                .andExpect(jsonPath("$.data.username").value("username"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 401 when credentials are invalid")
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("password")
                .build();

        when(loginRateLimiter.isAllowed(any())).thenReturn(true);

        when(authService.login(any(LoginRequest.class))).thenThrow(BadCredentialsException.class);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 429 when rate limit exceed")
    void shouldReturn429WhenRateLimitExceed() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("password")
                .build();

        when(loginRateLimiter.isAllowed(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("RATE_LIMIT_EXCEEDED"));
    }
}
