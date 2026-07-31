package com.ali.docqa.repository;

import com.ali.docqa.dto.RetrievedChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data access for document_chunks — driven by plain SQL (JdbcTemplate) because pgvector's
 * `vector` type isn't a standard JPA type. Kept out of JPA entirely, so Hibernate never touches it.
 */
@Repository
public class ChunkRepository {

    private final JdbcTemplate jdbc;

    public ChunkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Insert one chunk. The embedding is passed as pgvector's text literal and cast with ::vector. */
    public void save(long documentId, int chunkIndex, String content, float[] embedding) {
        jdbc.update(
                "INSERT INTO document_chunks (document_id, chunk_index, content, embedding) " +
                        "VALUES (?, ?, ?, ?::vector)",
                documentId, chunkIndex, content, toVectorLiteral(embedding));
    }

    /**
     * Find the k chunks whose embeddings are nearest (most similar) to the query vector — scoped to
     * a single user, and optionally to a chosen subset of that user's documents.
     *
     *   JOIN documents d ... WHERE d.user_id = ?  -> TENANCY: a user can only ever retrieve chunks
     *                                                from their OWN documents (never another user's).
     *   AND c.document_id IN (...)                -> optional: restrict to the documents the user
     *                                                selected in the UI. Empty/null = all their docs.
     *   embedding <=> ?::vector                   -> pgvector cosine distance (HNSW index). Smaller = closer.
     *
     * document_id + chunk_index come back with every hit, so provenance (for citations) is preserved.
     */
    public List<RetrievedChunk> search(float[] queryVector, int k, Long userId, List<Long> documentIds) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.document_id, c.chunk_index, c.content, c.embedding <=> ?::vector AS distance " +
                        "FROM document_chunks c " +
                        "JOIN documents d ON d.id = c.document_id " +
                        "WHERE d.user_id = ? ");
        List<Object> args = new ArrayList<>();
        args.add(toVectorLiteral(queryVector));
        args.add(userId);

        if (documentIds != null && !documentIds.isEmpty()) {
            sql.append("AND c.document_id IN (")
                    .append(String.join(",", Collections.nCopies(documentIds.size(), "?")))
                    .append(") ");
            args.addAll(documentIds);
        }

        sql.append("ORDER BY distance LIMIT ?");
        args.add(k);

        return jdbc.query(sql.toString(),
                (rs, rowNum) -> new RetrievedChunk(
                        rs.getLong("document_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("content"),
                        rs.getDouble("distance")),
                args.toArray());
    }

    /** float[] {0.1, 0.2, 0.3}  ->  the string "[0.1,0.2,0.3]" that pgvector understands. */
    private String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
