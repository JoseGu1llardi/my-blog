package com.joseguillard.my_blog.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    private String token;
    private String username;
    private String fullName;
}
