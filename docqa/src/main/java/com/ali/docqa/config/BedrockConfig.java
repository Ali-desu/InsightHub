package com.ali.docqa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * The AWS Bedrock Runtime client — used to invoke Titan for embeddings (and later Claude).
 * Same credential story as S3: DefaultCredentialsProvider reads ~/.aws/credentials; nothing secret
 * lives in the repo. Region is separate from S3's (Bedrock could live in a different region).
 */
@Configuration
public class BedrockConfig {

    @Value("${aws.bedrock.region}")
    private String region;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
