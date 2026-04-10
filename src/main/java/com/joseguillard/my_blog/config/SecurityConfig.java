package com.joseguillard.my_blog.config;

import com.joseguillard.my_blog.security.JwtAuthenticationFilter;
import com.joseguillard.my_blog.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configures a stateless security chain with public routes and JWT filter
        http
            .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> {
                    if ("dev".equals(activeProfile)) {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                    } else {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    }
                })
                .authorizeHttpRequests(auth -> {
                    auth
                         // Public routes
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/authors/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();

                    if ("dev".equals(activeProfile)) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }
                    // Everything else requires authentication
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((
                                request,
                                response,
                                authException) -> {
                                    response.setContentType("application/json");
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write(
                                    "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}"
                            );
                                } ))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

}
