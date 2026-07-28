package com.ali.docqa.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the RAG query endpoint: just the user's natural-language question.
 */
public record AskRequest(
        @NotBlank String question
) {
}
