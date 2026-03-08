package com.joseguillard.my_blog.exception;

public class PostStateConflictException extends RuntimeException {
    public PostStateConflictException(String message) {
        super(message);
    }
}
