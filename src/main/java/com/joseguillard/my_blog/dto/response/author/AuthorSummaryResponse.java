package com.joseguillard.my_blog.dto.response.author;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorSummaryResponse {

    private Long id;
    private String fullName;
    private String slug;
    private String avatarUrl;
}
