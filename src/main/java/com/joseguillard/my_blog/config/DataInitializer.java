package com.joseguillard.my_blog.config;

import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.Post;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.entity.vo.Slug;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initializes a database with author and post if empty
     */
    @Override
    public void run(String... args) {
        if (authorRepository.count() > 0 && postRepository.count() > 0) return;

        // Builds author with encoded password and metadata
        Author author = Author.builder()
                .userName("Jose Guillard")
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

        // Builds post with metadata and content
        Post post1 = Post.builder()
                .title("Java and Spring Boot")
                .slug(Slug.of("java-and-spring-boot"))
                .content("Content of the first post about Java and Spring Boot...")
                .excerpt("An Introduction to Java and Spring Boot Development")
                .status(PostStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .viewsCount(0)
                .author(author)
                .categories(Collections.emptySet())
                .build();

        // Builds the second published post with the current timestamp
        Post post2 = Post.builder()
                .title("Dirty Checking in Hibernate")
                .slug(Slug.of("dirty-checking-in-hibernate"))
                .content("Post content about dirty checking...")
                .excerpt("Understand how Hibernate automatically detects changes")
                .status(PostStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .viewsCount(0)
                .author(author)
                .categories(Collections.emptySet())
                .build();

        postRepository.saveAll(List.of(post1, post2));
    }
}
