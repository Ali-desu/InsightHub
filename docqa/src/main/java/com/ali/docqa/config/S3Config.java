package com.ali.docqa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Wires the single AWS object we need: an {@link S3Presigner}, which generates presigned URLs.
 *
 * Credentials are NOT hardcoded here. DefaultCredentialsProvider looks them up from the standard
 * places (env vars AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY, or ~/.aws/credentials), so no secret
 * ever lives in this repo.
 */
@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /** Server-side S3 client — used to download an uploaded object during ingestion. */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
