package com.ali.docqa.event;

/**
 * Published when an upload is confirmed. Its only job is to decouple "the file is in S3" from the
 * slow ingestion work — today an in-process Spring event, later this same seam becomes a Kafka/SQS
 * message consumed by a separate ingestion service.
 */
public record DocumentUploaded(Long documentId) {
}
