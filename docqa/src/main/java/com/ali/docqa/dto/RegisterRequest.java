package com.ali.docqa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating an account. The raw password arrives here, gets hashed in the
 * service, and is never stored as-is.
 */
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
