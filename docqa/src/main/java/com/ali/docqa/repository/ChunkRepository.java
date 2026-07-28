package com.ali.docqa.repository;

import com.ali.docqa.dto.RetrievedChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
     * Find the k chunks whose embeddings are nearest (most semantically similar) to the query vector.
     *
     *   embedding <=> ?::vector   -> pgvector's COSINE-distance operator (matches our vector_cosine_ops
     *                               HNSW index). Smaller = more similar.
     *   ORDER BY distance LIMIT k -> the k closest chunks. The HNSW index makes this fast.
     *
     * We SELECT document_id + chunk_index too, so every hit still knows which document/position it
     * came from — that provenance is what powers citations later.
     */
    public List<RetrievedChunk> search(float[] queryVector, int k) {
        return jdbc.query(
                "SELECT document_id, chunk_index, content, embedding <=> ?::vector AS distance " +
                        "FROM document_chunks " +
                        "ORDER BY distance " +
                        "LIMIT ?",
                (rs, rowNum) -> new RetrievedChunk(
                        rs.getLong("document_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("content"),
                        rs.getDouble("distance")),
                toVectorLiteral(queryVector), k);
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
