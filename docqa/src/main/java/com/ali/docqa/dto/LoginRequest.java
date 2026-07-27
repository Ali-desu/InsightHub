package com.ali.docqa.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for logging in. The raw password is checked against the stored hash; never stored.
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
