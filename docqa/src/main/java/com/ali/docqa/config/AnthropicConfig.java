package com.ali.docqa.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the official Anthropic Java SDK client as a Spring bean.
 *
 * The API key is read from a local .env file (docqa/.env) via dotenv-java, which also falls back
 * to a real OS environment variable of the same name. Spring Boot does NOT read .env on its own,
 * so we load it explicitly here. The .env file is gitignored — the key never enters version control.
 */
@Configuration
public class AnthropicConfig {

    @Bean
    AnthropicClient anthropicClient() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String apiKey = dotenv.get("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY not found. Add it to docqa/.env (ANTHROPIC_API_KEY=sk-ant-...) "
                            + "or set it as an environment variable.");
        }
        return AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }
}
