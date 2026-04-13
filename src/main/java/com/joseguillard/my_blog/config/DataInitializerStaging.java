package com.joseguillard.my_blog.config;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("staging")
@RequiredArgsConstructor
public class DataInitializerStaging implements CommandLineRunner {

    @Value("${app.init.password}")
    private String authorPassword;

    @Value("${app.init.email}")
    private String authorEmail;

    private final AuthorRepository authorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (authorRepository.count() > 0) return;

        Author author = Author.builder()
                .userName("gr1llard")
                .email(Email.of(authorEmail))
                .password(passwordEncoder.encode(authorPassword))
                .fullName("Jose Wellington")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        authorRepository.save(author);
    }
}
