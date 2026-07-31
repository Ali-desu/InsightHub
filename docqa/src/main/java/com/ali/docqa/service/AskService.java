package com.ali.docqa.service;

import com.ali.docqa.dto.AskResponse;
import com.ali.docqa.dto.RetrievedChunk;
import com.ali.docqa.model.User;
import com.ali.docqa.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The RAG read path in one method — the mirror image of IngestionService.ingest():
 * ingest turns a document INTO vectors; ask turns a question into a vector and searches BY it.
 */
@Service
public class AskService {

    /** Chunks handed to Claude as context. */
    private static final int TOP_K = 12;
    /** Over-fetch a bit so we can drop boilerplate (table-of-contents) chunks before capping at TOP_K. */
    private static final int FETCH_K = 20;

    private final EmbeddingClient embeddingClient;
    private final ChunkRepository chunkRepository;
    private final ClaudeClient claudeClient;

    public AskService(EmbeddingClient embeddingClient,
                      ChunkRepository chunkRepository,
                      ClaudeClient claudeClient) {
        this.embeddingClient = embeddingClient;
        this.chunkRepository = chunkRepository;
        this.claudeClient = claudeClient;
    }

    /**
     * Answer a question over the user's documents.
     *
     * @param user        the authenticated user — retrieval is scoped to THEIR documents only (tenancy).
     * @param documentIds optional subset of documents to search; null/empty means all of the user's.
     */
    public AskResponse ask(String question, User user, List<Long> documentIds) {
        float[] queryVector = embeddingClient.embed(question);

        // Over-fetch, then drop table-of-contents/index chunks. These match section titles
        // semantically ("Conclusion Générale ......... 75") but carry no real content — the reason
        // "what was the conclusion" used to return "it's only in the table of contents".
        List<RetrievedChunk> hits = chunkRepository.search(queryVector, FETCH_K, user.getId(), documentIds);
        if (hits.isEmpty()) {
            return new AskResponse("I couldn't find anything relevant in your documents.", List.of());
        }

        List<RetrievedChunk> ranked = hits.stream()
                .filter(chunk -> !looksLikeTableOfContents(chunk.content()))
                .limit(TOP_K)
                .toList();
        // Safety net: if every hit looked like boilerplate, fall back to the raw top-K.
        if (ranked.isEmpty()) {
            ranked = hits.stream().limit(TOP_K).toList();
        }

        return claudeClient.answer(question, ranked);
    }

    /**
     * A chunk is treated as a table of contents / index when it's dominated by dotted-leader lines,
     * e.g. "Conclusion Générale ...................... 75". Such chunks are noise for Q&A.
     */
    private static boolean looksLikeTableOfContents(String content) {
        String[] lines = content.split("\\R");
        int total = 0;
        int tocLines = 0;
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            total++;
            // runs of dots (".....") or spaced dot leaders (". . .") — classic TOC formatting
            if (t.matches(".*\\.{4,}.*") || t.matches(".*(\\.\\s){3,}.*")) {
                tocLines++;
            }
        }
        return total >= 4 && (tocLines * 100 / total) >= 30;
    }
}
