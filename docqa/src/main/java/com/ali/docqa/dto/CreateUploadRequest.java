package com.ali.docqa.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for "I want to upload a file" (metadata only — never the bytes).
 * The owner comes from the authenticated JWT, not the request body.
 */
public record CreateUploadRequest(
        @NotBlank String filename,
        @NotBlank String contentType
) {
}
