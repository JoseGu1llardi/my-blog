package com.joseguillard.my_blog.dto.response.author;

import com.joseguillard.my_blog.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {

    private Long id;
    private String username;
    private String fullName;
    private String slug;
    private String bio;
    private String avatarUrl;
    private String website;
    private String github;
    private String x;
    private String linkedin;
    private UserRole role;
    private boolean active;
    private long postCount;
}
