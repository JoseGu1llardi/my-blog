package com.joseguillard.my_blog.integration;

import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PostApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Author author;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";

        postRepository.deleteAll();
        authorRepository.deleteAll();

        author = Author.builder()
                .userName("joseguillard")
                .email(Email.of("junior@junior.com"))
                .password("joseguillard")
                .fullName("Jose Guillard")
                .role(UserRole.AUTHOR)
                .active(true)
                .build();
        authorRepository.save(author);
    }

    @Test
    @DisplayName("Full flow: create, search, update and delete a post")
    void shouldExecuteCompletePostFlow() {
        // Create post
        PostCreateRequest createRequest = PostCreateRequest.builder()
                .title("Integration Test Post")
                .content("This is an Integration Test Post")
                .excerpt("Summary of Integration Test Post")
                .status(PostStatus.PUBLISHED)
                .build();

        String slug = given()
                .contentType(ContentType.JSON)
                .queryParam("authorId", author.getId())
                .body(createRequest)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("message", containsString("created"))
                .body("data.slug", containsString("integration-test-post"))
                .body("data.title", containsString("Integration Test Post"))
                .body("data.status", equalTo("PUBLISHED"))
                .extract()
                .path("data.slug");
    }
}
