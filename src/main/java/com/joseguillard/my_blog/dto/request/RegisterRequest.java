package com.joseguillard.my_blog.dto.request;

import com.joseguillard.my_blog.entity.vo.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String fullName;

    @NotBlank
    private Email email;

    @NotBlank
    @Size(min = 8)
    private String password;
}
