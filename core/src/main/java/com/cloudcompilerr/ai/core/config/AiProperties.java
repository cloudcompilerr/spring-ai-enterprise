package com.cloudcompilerr.ai.core.config;

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
 * Updated to use Java 21 features.
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
     * Using record instead of class for immutability and conciseness.
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
        
        /**
         * Returns model configuration as a ModelConfig record for immutable operations.
         * Using Java 21 records for immutable data transfer.
         */
        public ModelConfig asModelConfig() {
            return new ModelConfig(model, embeddingModel, temperature, maxTokens);
        }
    }
    
    /**
     * Immutable record for model configuration.
     * Using Java 21 record feature for concise immutable data classes.
     */
    public record ModelConfig(String model, String embeddingModel, float temperature, int maxTokens) {
        /**
         * Creates a new ModelConfig with a different temperature.
         * 
         * @param newTemperature The new temperature value
         * @return A new ModelConfig instance with the updated temperature
         */
        public ModelConfig withTemperature(float newTemperature) {
            return new ModelConfig(model, embeddingModel, newTemperature, maxTokens);
        }
        
        /**
         * Creates a new ModelConfig with different max tokens.
         * 
         * @param newMaxTokens The new max tokens value
         * @return A new ModelConfig instance with the updated max tokens
         */
        public ModelConfig withMaxTokens(int newMaxTokens) {
            return new ModelConfig(model, embeddingModel, temperature, newMaxTokens);
        }
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
        
        /**
         * Returns RAG configuration as a RagConfig record for immutable operations.
         * Using Java 21 records for immutable data transfer.
         */
        public RagConfig asRagConfig() {
            return new RagConfig(topK, similarityThreshold, chunkSize, chunkOverlap);
        }
    }
    
    /**
     * Immutable record for RAG configuration.
     * Using Java 21 record feature for concise immutable data classes.
     */
    public record RagConfig(int topK, float similarityThreshold, int chunkSize, int chunkOverlap) {
        /**
         * Creates a new RagConfig with a different topK value.
         * 
         * @param newTopK The new topK value
         * @return A new RagConfig instance with the updated topK
         */
        public RagConfig withTopK(int newTopK) {
            return new RagConfig(newTopK, similarityThreshold, chunkSize, chunkOverlap);
        }
        
        /**
         * Creates a new RagConfig with a different similarity threshold.
         * 
         * @param newThreshold The new similarity threshold
         * @return A new RagConfig instance with the updated threshold
         */
        public RagConfig withSimilarityThreshold(float newThreshold) {
            return new RagConfig(topK, newThreshold, chunkSize, chunkOverlap);
        }
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
        
        /**
         * Returns vector DB configuration as a VectorDbConfig record for immutable operations.
         * Using Java 21 records for immutable data transfer.
         */
        public VectorDbConfig asVectorDbConfig() {
            return new VectorDbConfig(type, dimension);
        }
    }
    
    /**
     * Immutable record for vector database configuration.
     * Using Java 21 record feature for concise immutable data classes.
     */
    public record VectorDbConfig(String type, int dimension) {
        /**
         * Creates a new VectorDbConfig with a different dimension.
         * 
         * @param newDimension The new dimension value
         * @return A new VectorDbConfig instance with the updated dimension
         */
        public VectorDbConfig withDimension(int newDimension) {
            return new VectorDbConfig(type, newDimension);
        }
    }
}