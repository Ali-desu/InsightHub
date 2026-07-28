package com.ali.docqa.service;

import com.ali.docqa.dto.AskResponse;
import com.ali.docqa.dto.RetrievedChunk;
import com.ali.docqa.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The RAG read path in one method — the mirror image of IngestionService.ingest():
 * ingest turns a document INTO vectors; ask turns a question into a vector and searches BY it.
 */
@Service
public class AskService {

    /** How many chunks to retrieve and hand to Claude as context. */
    private static final int TOP_K = 10;

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
     * ==========================================================================================
     *  YOURS TO WRITE — the query pipeline. Everything you need is injected above.
     *
     *  Steps:
     *   1. Embed the question into a query vector:
     *        float[] queryVector = embeddingClient.embed(question);
     *   2. Find the most relevant chunks (semantic search):
     *        List<RetrievedChunk> hits = chunkRepository.search(queryVector, TOP_K);
     *   3. (nice touch) if hits.isEmpty(), return
     *        new AskResponse("I couldn't find anything relevant in your documents.", List.of());
     *   4. Otherwise, let Claude write the grounded, cited answer:
     *        return claudeClient.answer(question, hits);
     *
     *  Delete the throw below once you've implemented it.
     * ==========================================================================================
     */
    public AskResponse ask(String question) {
        float[] queryVector = embeddingClient.embed(question);
        List<RetrievedChunk> hits = chunkRepository.search(queryVector, TOP_K);
        if (hits.isEmpty()) {
            return new AskResponse("I couldn't find anything relevant in your documents.", List.of());
        }
        return claudeClient.answer(question, hits);
    }
}
