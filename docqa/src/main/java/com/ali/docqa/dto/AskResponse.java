package com.ali.docqa.dto;

import java.util.List;

/**
 * Response from the RAG query endpoint: the generated answer plus the citations that back it up.
 * An empty citations list means the model answered without grounding in a specific chunk.
 */
public record AskResponse(
        String answer,
        List<Citation> citations
) {
}
