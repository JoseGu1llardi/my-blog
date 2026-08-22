package com.joseguillard.my_blog.dto.response.author;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {

    private String username;
    private String fullName;
    private String slug;
    private String bio;
    private String avatarUrl;
    private String website;
    private String github;
    private String x;
    private String linkedin;
    private long postCount;
}
