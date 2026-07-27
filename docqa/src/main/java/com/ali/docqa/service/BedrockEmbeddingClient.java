package com.ali.docqa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Map;

/**
 * Bedrock-backed embeddings via Amazon Titan Text Embeddings V2.
 *
 * The call is a generic Bedrock "invokeModel": you send a model-specific JSON body and get a
 * model-specific JSON body back. For Titan:
 *   request  -> {"inputText": "<text>"}
 *   response -> {"embedding": [ ...1024 floats... ], "inputTextTokenCount": N}
 */
@Service
public class BedrockEmbeddingClient implements EmbeddingClient {

    private final BedrockRuntimeClient bedrock;
    private final String modelId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BedrockEmbeddingClient(BedrockRuntimeClient bedrock,
                                  @Value("${aws.bedrock.embedding-model}") String modelId) {
        this.bedrock = bedrock;
        this.modelId = modelId;
    }

    @Override
    public float[] embed(String text) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of("inputText", text));

            InvokeModelResponse response = bedrock.invokeModel(request -> request
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody)));

            JsonNode root = objectMapper.readTree(response.body().asByteArray());
            JsonNode embedding = root.get("embedding");

            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = (float) embedding.get(i).asDouble();
            }
            return vector;
        } catch (Exception e) {
            throw new RuntimeException("Failed to embed text via Bedrock Titan", e);
        }
    }
}
