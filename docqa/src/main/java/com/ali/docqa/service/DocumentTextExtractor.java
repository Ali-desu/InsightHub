package com.ali.docqa.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.ByteArrayInputStream;

/**
 * Downloads an uploaded document from S3 and extracts its plain text.
 *
 * Tika auto-detects the format (PDF, plain text, docx, …) and returns text, so ingestion doesn't
 * need to special-case file types.
 */
@Component
public class DocumentTextExtractor {

    private final S3Client s3Client;
    private final String bucket;
    private final Tika tika = new Tika();

    public DocumentTextExtractor(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        // Tika's facade caps extracted text at 100k chars by default — raise it for real documents.
        this.tika.setMaxStringLength(10_000_000);
    }

    /** @param s3key the object key (the Document's s3key) → the document's full text. */
    public String extract(String s3key) {
        try {
            byte[] bytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(s3key).build()
            ).asByteArray();

            return tika.parseToString(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from s3://" + bucket + "/" + s3key, e);
        }
    }
}
