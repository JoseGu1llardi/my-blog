package com.joseguillard.my_blog.model.enums;

import lombok.Getter;

@Getter
public enum PostStatus {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    SCHEDULED("Scheduled"),
    DELETED("Deleted"),
    ARCHIVED("Archived");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }
}
