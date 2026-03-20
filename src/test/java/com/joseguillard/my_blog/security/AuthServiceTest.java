package com.joseguillard.my_blog.security;

import com.joseguillard.my_blog.dto.request.auth.LoginRequest;
import com.joseguillard.my_blog.dto.response.AuthResponse;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Author author;

    @BeforeEach
    public void setup() {
        author = Author.builder()
                .id(1L)
                .userName("joseguillard")
                .email(Email.of("junior11_junior@hotmail.com"))
                .fullName("Jose Guillard")
                .password("password")
                .role(UserRole.AUTHOR)
                .build();
    }

    @Test
    @DisplayName("Should authenticate user and return JWT token when credentials are valid")
    void shouldReturnTokenWhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("joseguillard")
                .password("password")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(author.getUsername(), author.getPassword()));

        // Author exists
        when(authorRepository.findByUserName("joseguillard")).thenReturn(Optional.of(author));

        when(jwtService.generateToken("joseguillard")).thenReturn("fake-token");

        AuthResponse result = authService.login(request);

        assertThat(result.getToken()).isEqualTo("fake-token");
        assertThat(result.getUsername()).isEqualTo("joseguillard");

        InOrder inOrder = inOrder(authenticationManager, authorRepository, jwtService);

        inOrder.verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        inOrder.verify(authorRepository).findByUserName("joseguillard");
        inOrder.verify(jwtService).generateToken(author.getUsername());
    }
}
