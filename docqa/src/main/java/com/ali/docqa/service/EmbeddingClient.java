package com.ali.docqa.service;

/**
 * One job: turn a piece of text into an embedding vector. Behind an interface so the rest of the
 * app (ingestion, query) never knows or cares which provider produces the vector.
 */
public interface EmbeddingClient {

    /** @return the embedding as a float array (Titan v2 default = 1024 dimensions). */
    float[] embed(String text);
}
