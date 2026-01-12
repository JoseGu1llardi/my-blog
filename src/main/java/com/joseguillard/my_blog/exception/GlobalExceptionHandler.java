package com.joseguillard.my_blog.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns a ModelAndView to display a custom 404 error page.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        mav.addObject("path",  request.getRequestURI());
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    /**
     * Responds with error details for invalid slugs
     */
    @ExceptionHandler(InvalidSlugException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSlug(InvalidSlugException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("message", ex.getMessage());
        error.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(error);
    }
}
