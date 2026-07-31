package com.ali.docqa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures the pgvector extension, the document_chunks table, and its HNSW index exist at startup.
 *
 * document_chunks is NOT a JPA entity (pgvector's `vector` type isn't a standard JPA type), so
 * Hibernate never creates it. This runner does — idempotently (IF NOT EXISTS). It runs after the
 * context is refreshed, i.e. after Hibernate has already created the `documents` table, so the
 * foreign key resolves. Result: the schema is reproducible on any fresh database (Docker, a new
 * environment) with no manual psql step.
 */
@Component
public class ChunkSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ChunkSchemaInitializer.class);

    private final JdbcTemplate jdbc;

    public ChunkSchemaInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        } catch (Exception e) {
            // On a locked-down DB the extension may already be installed by an admin; that's fine.
            log.warn("Could not ensure 'vector' extension (may already exist / needs superuser): {}",
                    e.getMessage());
        }

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS document_chunks (
                    id BIGSERIAL PRIMARY KEY,
                    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                    chunk_index INT NOT NULL,
                    content TEXT NOT NULL,
                    embedding vector(1024) NOT NULL
                )""");

        jdbc.execute("""
                CREATE INDEX IF NOT EXISTS idx_chunks_embedding
                ON document_chunks USING hnsw (embedding vector_cosine_ops)""");

        log.info("document_chunks schema ready (pgvector extension + table + HNSW index ensured)");
    }
}
