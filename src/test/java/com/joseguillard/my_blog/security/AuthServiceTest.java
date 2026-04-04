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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        // Simulates a valid login request coming from the client
        // This represents the real input of the method under the test
        LoginRequest request = LoginRequest.builder()
                .username("joseguillard")
                .password("password")
                .build();

        // Mock Spring Security authentication
        // We are NOT testing AuthenticationManager here
        // only assuming authentication succeeds (no exception thrown)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(author.getUsername(), author.getPassword()));

        // Mock repository behavior: user exists in the database
        // This allows the login flow to continue after authentication
        when(authorRepository.findByUserName("joseguillard")).thenReturn(Optional.of(author));

        // Mock JWT generation
        // We do not care about the real token implementation here,
        // only that a token is generated and returned correctly
        when(jwtService.generateToken("joseguillard", 1)).thenReturn("fake-token");

        // Act

        // Call the real method under the test
        AuthResponse result = authService.login(request);

        // Assert

        // Verify that the generated token matches the expected value
        // This confirms that JWT was triggered correctly
        assertThat(result.getToken()).isEqualTo("fake-token");

        // Varify that the correct user data is returned
        assertThat(result.getUsername()).isEqualTo("joseguillard");

        // Ensure the login flow follows the expected order
        InOrder inOrder = inOrder(authenticationManager, authorRepository, jwtService);

        // 1 - Authenticate credentials
        inOrder.verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // 2 - Fetch user from a repository
        inOrder.verify(authorRepository).findByUserName("joseguillard");

        // 3 - Generate JWT token
        inOrder.verify(jwtService).generateToken(author.getUsername(), 1);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when credentials are invalid")
    void shouldThrowBadCredentialsExceptionWhenCredentialsAreInvalid() {
        LoginRequest request = LoginRequest.builder()
                .username("joseguillard")
                .password("password")
                .build();

        // Mock the authentication failure
        // This simulates a core behavior of Spring Security when credentials are invalid:
        // the AuthenticationManager throws a BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Execute the login and verify that the expected exception is thrown
        // This confirms that AuthService correctly propagates authentication failures
        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);

        // Ensure the authentication was attempted
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Ensure that no further processing happens after authentication fails:
        // the system must not query the database
        verify(authorRepository, never()).findByUserName(anyString());

        // The system must NOT generate a JWT token
        verify(jwtService, never()).generateToken(anyString(), anyInt());
    }
}
