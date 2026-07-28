package com.ali.docqa.service;

import com.ali.docqa.event.DocumentUploaded;
import com.ali.docqa.model.Document;
import com.ali.docqa.repository.ChunkRepository;
import com.ali.docqa.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Turns an uploaded document into searchable vectors: extract text -> chunk -> embed -> store.
 * Runs off the request thread (@Async) because embedding many chunks is slow.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractor extractor;
    private final TextChunker chunker;
    private final EmbeddingClient embeddingClient;
    private final ChunkRepository chunkRepository;

    public IngestionService(DocumentRepository documentRepository,
                            DocumentTextExtractor extractor,
                            TextChunker chunker,
                            EmbeddingClient embeddingClient,
                            ChunkRepository chunkRepository) {
        this.documentRepository = documentRepository;
        this.extractor = extractor;
        this.chunker = chunker;
        this.embeddingClient = embeddingClient;
        this.chunkRepository = chunkRepository;
    }

    /** Fires on a background thread when confirmUpload publishes DocumentUploaded. */
    @Async
    @EventListener
    public void onDocumentUploaded(DocumentUploaded event) {
        ingest(event.documentId());
    }

    public void ingest(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        try {
            document.setStatus(Document.DocumentStatus.PROCESSING);
            documentRepository.save(document);

            String text = extractor.extract(document.getS3key());
            List<String> chunks = chunker.chunk(text);
            for (int i = 0; i < chunks.size(); i++) {
                float[] vector = embeddingClient.embed(chunks.get(i));
                chunkRepository.save(documentId, i, chunks.get(i), vector);
                log.info("Indexed chunk {}", i);
            }
            document.setStatus(Document.DocumentStatus.INDEXED);
            documentRepository.save(document);
        } catch (Exception e) {
            log.error("Error occurred while ingesting document {}", documentId, e);
            document.setStatus(Document.DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }
}
