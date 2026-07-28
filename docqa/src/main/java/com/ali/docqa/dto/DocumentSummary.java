package com.ali.docqa.dto;

/**
 * Lightweight view of a document for the sidebar list — no S3 key or internals leaked.
 */
public record DocumentSummary(
        Long id,
        String filename,
        String status,
        String mimetype
) {
}
