package com.ali.docqa.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request body for the RAG query endpoint.
 *
 * @param question    the user's natural-language question.
 * @param documentIds optional: restrict the search to these documents. Null or empty = search all
 *                    of the current user's documents.
 */
public record AskRequest(
        @NotBlank String question,
        List<Long> documentIds
) {
}
