-- Runs once, on first initialization of the Postgres data volume.
-- Enables pgvector so the backend can create the document_chunks table (embedding vector(1024)).
-- The table + HNSW index themselves are created by the app (ChunkSchemaInitializer) at startup,
-- after Hibernate has created the referenced `documents` table.
CREATE EXTENSION IF NOT EXISTS vector;
