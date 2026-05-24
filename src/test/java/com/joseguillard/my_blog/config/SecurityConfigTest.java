package com.joseguillard.my_blog.config;

import com.joseguillard.my_blog.controller.PostController;
import com.joseguillard.my_blog.security.JwtAuthenticationFilter;
import com.joseguillard.my_blog.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostController.class)
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtAuthenticationFilter jwtAuthFilter;

    @MockBean UserDetailsServiceImpl userDetailsService;
}
