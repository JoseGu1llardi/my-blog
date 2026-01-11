package com.joseguillard.my_blog.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException postNotFound(String slug) {
        return new ResourceNotFoundException(
                String.format("Post with slug '%s' not found", slug)
        );
    }

    public static ResourceNotFoundException authorNotFound(String slug) {
        return new ResourceNotFoundException(
                String.format("Author with slug '%s' not found", slug)
        );
    }

    public static ResourceNotFoundException categoryNotFound(String slug) {
        return new ResourceNotFoundException(
                String.format("Category with slug '%s' not found", slug)
        );
    }
}
