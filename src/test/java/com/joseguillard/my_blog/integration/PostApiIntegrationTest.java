package com.joseguillard.my_blog.integration;

import com.joseguillard.my_blog.dto.request.post.PostCreateRequest;
import com.joseguillard.my_blog.dto.request.post.PostUpdateRequest;
import com.joseguillard.my_blog.entity.Author;
import com.joseguillard.my_blog.entity.enums.PostStatus;
import com.joseguillard.my_blog.entity.enums.UserRole;
import com.joseguillard.my_blog.entity.vo.Email;
import com.joseguillard.my_blog.exception.ApiErrorType;
import com.joseguillard.my_blog.repository.AuthorRepository;
import com.joseguillard.my_blog.repository.PostRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Author author;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";

        jdbcTemplate.execute("DELETE FROM post_categories");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM authors");

        author = Author.builder()
                .userName("joseguillard")
                .email(Email.of("junior@junior.com"))
                .password(passwordEncoder.encode("joseguillard"))
                .fullName("Jose Guillard")
                .role(UserRole.AUTHOR)
                .active(true)
                .build();
        authorRepository.save(author);

        // Fetch token AFTER RestAssured is configured and author is saved
        this.token = getToken();
    }

    @Test
    @DisplayName("Full flow: create, search, update and delete a post")
    void shouldExecuteCompletePostFlow() {
        // Create post
        PostCreateRequest createRequest = PostCreateRequest.builder()
                .title("Integration Test Post")
                .content("This is an Integration Test Post")
                .excerpt("Summary of Integration Test Post")
                .build();

        ExtractableResponse<Response> createResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
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
                .body("data.status", equalTo("DRAFT"))
                .extract();

        String slug =  createResponse.path("data.slug").toString();
        int id = createResponse.path("data.id");

        // Publish post
        given()
            .header("Authorization", "Bearer " + token)
                  .when()
                      .patch("/posts/" + id + "/publish")
                  .then()
                      .statusCode(204);

        // Search created post
        given()
            .when()
                .get("/posts/" + slug)
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.title", equalTo("Integration Test Post"))
                .body("data.content", equalTo("This is an Integration Test Post"))
                .body("data.viewCount", equalTo(0))
            .extract()
               .path("data.id");

        // List posts
        given()
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/posts")
        .then()
            .statusCode(200)
                .body("success", equalTo(true))
                .body("data.content", hasSize(1))
                .body("data.totalElements", equalTo(1));

        // Search again should increase views
        given()
        .when()
            .get("/posts/" + slug)
        .then()
            .statusCode(200)
                .body("success", equalTo(true))
                .body("data.viewCount", equalTo(1));

        // Update post
        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title("Integration Test Post")
                .slug("new-slug-update")
                .content("This is an updating Integration Test Post")
                .excerpt("Updating the excerpt")
                .build();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(updateRequest)
        .when()
            .put("/posts/" + id)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.title", equalTo("Integration Test Post"))
                .body("data.content", equalTo("This is an updating Integration Test Post"))
                .body("data.slug", equalTo("new-slug-update"));

        // Delete a post
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/posts/" + id)
        .then()
            .statusCode(204);

        // Verify if it´s gone
        given()
        .when()
            .get("/posts/" + slug)
        .then()
            .statusCode(404)
            .body("error", equalTo(ApiErrorType.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("Should return 404 when searching for missing post")
    void shouldReturn404ForNonExistingPost() {
        given()
        .when()
            .get("/posts/post-does-not-exist")
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("error", equalTo(ApiErrorType.RESOURCE_NOT_FOUND.name()))
            .body("message", containsString("not found"));
    }

    @Test
    @DisplayName("Should validate required fields to create post")
    void shouldValidateRequiredFields() {
        PostCreateRequest invalidRequest = PostCreateRequest.builder()
                .content("This is an Integration Test Post")
                .build();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .queryParam("authorId", author.getId())
            .body(invalidRequest)
        .when()
            .post("/posts")
        .then()
            .statusCode(400)
            .body("error", equalTo(ApiErrorType.VALIDATION_ERROR.name()))
            .body("errors[0].field", equalTo("title"));
    }

    private String getToken() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"joseguillard\",\"password\":\"joseguillard\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("data.token");
    }
}
