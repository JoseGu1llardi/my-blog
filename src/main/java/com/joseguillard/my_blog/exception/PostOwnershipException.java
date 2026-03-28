package com.joseguillard.my_blog.exception;


import org.springframework.security.access.AccessDeniedException;

public class PostOwnershipException extends AccessDeniedException {
    public PostOwnershipException() {
        super("You do not have permission to modify this post");
    }
}
