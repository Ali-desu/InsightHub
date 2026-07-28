package com.ali.docqa.dto;

/**
 * One chunk returned by a similarity search, carrying its provenance and how close it was.
 *
 *   documentId / chunkIndex -> where this text came from (used later for citations)
 *   content                 -> the chunk text itself (fed to Claude as a source)
 *   distance                -> pgvector cosine distance to the question (0 = identical meaning,
 *                              closer to 0 = more relevant). Handy for debugging / thresholds.
 */
public record RetrievedChunk(
        long documentId,
        int chunkIndex,
        String content,
        double distance
) {
}
