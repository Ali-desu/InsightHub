package com.ali.docqa.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Splits a document's full text into overlapping fixed-size chunks. Each chunk becomes one embedding
 * and one row in document_chunks.
 */
@Component
public class TextChunker {

    private static final int CHUNK_SIZE = 1000; // characters per chunk
    private static final int OVERLAP = 200;     // characters each chunk shares with the previous one

    public List<String> chunk(String text) {
        int i = 0;
        List<String> chunks = new java.util.ArrayList<>();
        while (i < text.length()) {
            int end = Math.min(i + CHUNK_SIZE, text.length());
            String chunk = text.substring(i, end);
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            i += (CHUNK_SIZE - OVERLAP);
        }
        return chunks;
    }
}
