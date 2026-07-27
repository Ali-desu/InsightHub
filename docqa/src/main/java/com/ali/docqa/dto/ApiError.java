package com.ali.docqa.dto;

import java.time.Instant;

/** Uniform JSON error body returned by the exception handlers. */
public record ApiError(Instant timestamp, int status, String message) {
    public static ApiError of(int status, String message) {
        return new ApiError(Instant.now(), status, message);
    }
}
