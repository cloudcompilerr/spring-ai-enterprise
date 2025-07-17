package com.example.ai.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for AI services.
 * This class centralizes all AI-related configuration properties.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * OpenAI configuration properties.
     */
    @NotNull
    private OpenAi openai = new OpenAi();

    /**
     * RAG configuration properties.
     */
    @NotNull
    private Rag rag = new Rag();

    /**
     * Vector database configuration properties.
     */
    @NotNull
    private VectorDb vectorDb = new VectorDb();

    /**
     * OpenAI specific configuration.
     */
    @Data
    public static class OpenAi {
        /**
         * The model to use for chat completions.
         */
        @NotBlank
        private String model = "gpt-3.5-turbo";
        
        /**
         * The model to use for embeddings.
         */
        @NotBlank
        private String embeddingModel = "text-embedding-ada-002";
        
        /**
         * Temperature parameter for controlling randomness.
         */
        private float temperature = 0.7f;
        
        /**
         * Maximum number of tokens to generate.
         */
        @Positive
        private int maxTokens = 500;
    }

    /**
     * RAG specific configuration.
     */
    @Data
    public static class Rag {
        /**
         * Number of similar documents to retrieve.
         */
        @Positive
        private int topK = 3;
        
        /**
         * Similarity threshold for document retrieval.
         */
        private float similarityThreshold = 0.7f;
        
        /**
         * Maximum chunk size for document splitting.
         */
        @Positive
        private int chunkSize = 1000;
        
        /**
         * Chunk overlap size for document splitting.
         */
        @Positive
        private int chunkOverlap = 200;
    }

    /**
     * Vector database configuration.
     */
    @Data
    public static class VectorDb {
        /**
         * The type of vector database to use.
         */
        @NotBlank
        private String type = "pgvector";
        
        /**
         * The dimension of the embedding vectors.
         */
        @Positive
        private int dimension = 1536;
    }
}