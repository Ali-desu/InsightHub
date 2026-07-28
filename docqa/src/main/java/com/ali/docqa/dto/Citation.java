package com.ali.docqa.dto;

/**
 * One citation attached to an answer: a quote from a source chunk, plus where it came from.
 * The (documentId, chunkIndex) is the provenance we've carried all the way from ingestion —
 * this is what lets the UI point the user back at the exact source passage.
 */
public record Citation(
        long documentId,
        int chunkIndex,
        String quotedText
) {
}
