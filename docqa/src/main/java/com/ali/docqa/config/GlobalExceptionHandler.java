package com.ali.docqa.config;

import com.ali.docqa.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns exceptions thrown from controllers into clean JSON responses returned DIRECTLY.
 *
 * Without this, an unhandled exception forwards to the built-in /error endpoint, which re-enters
 * the Spring Security filter chain, is not in permitAll, and gets masked as a confusing 403.
 * Handling it here returns a real ResponseEntity, so there's no /error forward and no 403.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Used for "username taken", "invalid username or password", "not found", etc.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400
        return ResponseEntity.status(status).body(ApiError.of(status.value(), ex.getMessage()));
    }
}
