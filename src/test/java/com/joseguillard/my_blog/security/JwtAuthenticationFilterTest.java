package com.joseguillard.my_blog.security;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should not authenticate user when Authorization header is null")
    void shouldPassThroughWhenNotAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should not authenticate user when Authorization header does not start with Bearer")
    void shouldPassThroughWhenAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("XYZ2A5SSS8T7T4T1");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should authenticate user when token is valid and tokenVersion matches")
    void shouldAuthenticateWhenTokenIsValidAndTokenVersionMatches() throws Exception {
        // Arrange
        Author author = Author.builder()
                .userName("gr1llard")
                .password("hashed-password")
                .role(UserRole.AUTHOR)
                .active(true)
                .tokenVersion(1)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtService.extractUsername("valid.token.here")).thenReturn("gr1llard");
        when(userDetailsService.loadUserByUsername("gr1llard")).thenReturn(author);
        when(jwtService.isTokenValid("valid.token.here", author)).thenReturn(true);
        when(jwtService.extractTokenVersion("valid.token.here")).thenReturn(1);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert - SecurityContext must have authentication
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("gr1llard");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsValidAndTokenVersionDoesNotMatch() throws Exception {
        // Arrange
        Author author = Author.builder()
                .userName("gr1llard")
                .password("hashed-password")
                .role(UserRole.AUTHOR)
                .active(true)
                .tokenVersion(1)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtService.extractUsername("valid.token.here")).thenReturn("gr1llard");
        when(userDetailsService.loadUserByUsername("gr1llard")).thenReturn(author);
        when(jwtService.isTokenValid("valid.token.here", author)).thenReturn(true);
        when(jwtService.extractTokenVersion("valid.token.here")).thenReturn(2);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert - SecurityContext must have authentication
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
