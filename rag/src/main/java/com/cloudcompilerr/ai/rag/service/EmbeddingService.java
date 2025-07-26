package com.cloudcompilerr.ai.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Production-ready embedding service with batching, retry logic, and memory management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingClient embeddingClient;
    private final ExecutorService embeddingExecutor = Executors.newFixedThreadPool(3);
    
    // Configuration constants
    private static final int MAX_BATCH_SIZE = 5;
    private static final int MAX_TEXT_LENGTH = 8000; // OpenAI token limit
    private static final int RETRY_DELAY_MS = 1000;
    private static final int MAX_RETRIES = 3;

    /**
     * Creates embeddings for multiple text chunks asynchronously with batching.
     *
     * @param textChunks List of text chunks to embed
     * @return CompletableFuture containing list of embedding arrays
     */
    public CompletableFuture<List<float[]>> createEmbeddingsAsync(List<String> textChunks) {
        return CompletableFuture.supplyAsync(() -> {
            List<float[]> allEmbeddings = new ArrayList<>();
            
            // Process in batches to manage memory
            for (int i = 0; i < textChunks.size(); i += MAX_BATCH_SIZE) {
                int endIndex = Math.min(i + MAX_BATCH_SIZE, textChunks.size());
                List<String> batch = textChunks.subList(i, endIndex);
                
                log.debug("Processing embedding batch {}/{} with {} chunks", 
                         (i / MAX_BATCH_SIZE) + 1, 
                         (textChunks.size() + MAX_BATCH_SIZE - 1) / MAX_BATCH_SIZE,
                         batch.size());
                
                List<float[]> batchEmbeddings = processBatchWithRetry(batch);
                allEmbeddings.addAll(batchEmbeddings);
                
                // Memory management between batches
                if (endIndex < textChunks.size()) {
                    forceGarbageCollection();
                    sleepBetweenBatches();
                }
            }
            
            return allEmbeddings;
        }, embeddingExecutor);
    }

    /**
     * Creates a single embedding with retry logic and memory optimization.
     *
     * @param text Text to embed
     * @return Embedding array
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = MAX_RETRIES,
        backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = 2)
    )
    public float[] createEmbedding(String text) {
        try {
            // Truncate text if too long
            String processedText = text.length() > MAX_TEXT_LENGTH 
                ? text.substring(0, MAX_TEXT_LENGTH) 
                : text;
            
            log.debug("Creating embedding for text of length: {}", processedText.length());
            
            // Create embedding with memory monitoring
            long beforeMemory = getUsedMemory();
            List<Double> embeddingList = embeddingClient.embed(processedText);
            long afterMemory = getUsedMemory();
            
            log.debug("Embedding created. Memory used: {} MB", (afterMemory - beforeMemory) / (1024 * 1024));
            
            // Convert to float array efficiently
            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }
            
            // Clear the list to free memory
            embeddingList.clear();
            
            return embedding;
            
        } catch (Exception e) {
            log.error("Failed to create embedding for text (length: {}): {}", text.length(), e.getMessage());
            throw new RuntimeException("Embedding creation failed", e);
        }
    }

    /**
     * Processes a batch of text chunks with retry logic.
     */
    private List<float[]> processBatchWithRetry(List<String> batch) {
        List<float[]> embeddings = new ArrayList<>();
        
        for (String text : batch) {
            try {
                float[] embedding = createEmbedding(text);
                embeddings.add(embedding);
            } catch (Exception e) {
                log.error("Failed to process text in batch, using zero embedding: {}", e.getMessage());
                // Fallback to zero embedding to maintain consistency
                embeddings.add(createZeroEmbedding());
            }
        }
        
        return embeddings;
    }

    /**
     * Creates a zero embedding as fallback.
     */
    private float[] createZeroEmbedding() {
        float[] embedding = new float[1536]; // OpenAI embedding dimension
        // Array is already initialized with zeros
        return embedding;
    }

    /**
     * Forces garbage collection and logs memory usage.
     */
    private void forceGarbageCollection() {
        long beforeGC = getUsedMemory();
        System.gc();
        long afterGC = getUsedMemory();
        
        log.debug("Garbage collection: freed {} MB", (beforeGC - afterGC) / (1024 * 1024));
    }

    /**
     * Gets current used memory in bytes.
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Sleep between batches to prevent API rate limiting.
     */
    private void sleepBetweenBatches() {
        try {
            Thread.sleep(200); // 200ms delay between batches
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted between batches");
        }
    }

    /**
     * Shutdown the executor service gracefully.
     */
    public void shutdown() {
        embeddingExecutor.shutdown();
        try {
            if (!embeddingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                embeddingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            embeddingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}