package com.example.ai.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * Core configuration for Spring AI components.
 * This class provides the basic beans needed across the application.
 */
@Configuration
public class CoreConfig {

    /**
     * Creates an embedding client bean that can be used across the application.
     * 
     * @param openAiApi The OpenAI API client
     * @return An embedding client for generating embeddings
     */
    @Bean
    public EmbeddingClient embeddingClient(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingClient(openAiApi);
    }
}