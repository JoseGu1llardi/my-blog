package com.joseguillard.my_blog.entity.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("Administrator", "ROLE_ADMIN"),
    AUTHOR("Author", "ROLE_AUTHOR"),
    EDITOR("Editor", "ROLE_EDITOR"),;

    private final String  description;
    private final String authority;

    UserRole(String description, String authority) {
        this.description = description;
        this.authority = authority;
    }
}
