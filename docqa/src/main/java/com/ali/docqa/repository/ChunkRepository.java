package com.ali.docqa.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
