package com.joseguillard.my_blog.config;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (authorRepository.count() > 0) return;

        // Builds author with encoded password and metadata
        Author author = Author.builder()
                .userName("joseguillard")
                .email(Email.of("junior11_junior@hotmail.com"))
                .password(passwordEncoder.encode("joseguillard"))
                .fullName("Jose Wellington Ribeiro")
                .bio("Software engineer documenting my learning journey.")
                .avatarUrl("https://avatars.githubusercontent.com/u/63321040?v=4")
                .github("https://github.com/JoseGu1llardi")
                .linkedin("https://www.linkedin.com/in/joseguillard")
                .role(UserRole.AUTHOR)
                .active(true)
                .build();

        authorRepository.save(author);
    }
}
