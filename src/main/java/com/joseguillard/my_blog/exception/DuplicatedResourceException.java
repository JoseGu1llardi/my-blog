package com.joseguillard.my_blog.exception;

public class DuplicatedResourceException extends RuntimeException {
    public DuplicatedResourceException(String message) {
        super(message);
    }

    public static DuplicatedResourceException userNameAlreadyExists(String username) {
        return new DuplicatedResourceException(
                String.format("Username '%s' already exists", username)
        );
    }

    public static DuplicatedResourceException emailAlreadyExists(String email) {
        return new DuplicatedResourceException(
                String.format("Email '%s' already exists", email)
        );
    }
}
