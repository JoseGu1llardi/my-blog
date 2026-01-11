package com.joseguillard.my_blog.exception;

public class InvalidSlugException extends RuntimeException {
    public InvalidSlugException(String message) {
        super(message);
    }

    public InvalidSlugException(String message, Throwable cause) {
        super(message, cause);
    }
}
