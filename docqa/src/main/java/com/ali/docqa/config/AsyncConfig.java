package com.ali.docqa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async so ingestion runs on a background thread — the upload's confirm request returns
 * immediately instead of blocking on text extraction + embedding.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
