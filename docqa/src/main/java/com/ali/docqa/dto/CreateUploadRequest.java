package com.ali.docqa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for "I want to upload a file" (metadata only — never the bytes).
 *
 * NOTE: userId is temporary. Once JWT auth exists, the owner comes from the authenticated token,
 * not from the request body, and this field goes away.
 */
public record CreateUploadRequest(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull Long userId
) {
}
