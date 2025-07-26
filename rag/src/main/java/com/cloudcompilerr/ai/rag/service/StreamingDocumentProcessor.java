package com.cloudcompilerr.ai.rag.service;

import com.cloudcompilerr.ai.core.config.AiProperties;
import com.cloudcompilerr.ai.rag.model.Document;
import com.cloudcompilerr.ai.rag.model.DocumentChunk;
import com.cloudcompilerr.ai.rag.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Streaming document processor for handling large documents efficiently.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingDocumentProcessor {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final AiProperties aiProperties;

    /**
     * Processes a document using streaming approach with memory-efficient chunking.
     *
     * @param document The document to process
     * @return CompletableFuture containing the list of created chunks
     */
    public CompletableFuture<List<DocumentChunk>> processDocumentAsync(Document document) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting streaming processing for document: {} (length: {})", 
                        document.getId(), document.getContent().length());
                
                // Split document into chunks
                List<String> textChunks = splitDocumentIntoChunks(document.getContent());
                log.info("Document split into {} chunks", textChunks.size());
                
                // Process chunks in streaming fashion
                return processChunksStreaming(document, textChunks);
                
            } catch (Exception e) {
                log.error("Error in streaming document processing for document {}: {}", 
                         document.getId(), e.getMessage(), e);
                throw new RuntimeException("Streaming document processing failed", e);
            }
        });
    }

    /**
     * Splits document content into manageable chunks.
     */
    private List<String> splitDocumentIntoChunks(String content) {
        var ragConfig = aiProperties.getRag().asRagConfig();
        int chunkSize = ragConfig.chunkSize();
        int chunkOverlap = ragConfig.chunkOverlap();
        
        List<String> chunks = new ArrayList<>();
        int startIndex = 0;
        
        while (startIndex < content.length()) {
            int endIndex = Math.min(startIndex + chunkSize, content.length());
            
            // Adjust end index to avoid cutting words
            if (endIndex < content.length()) {
                int nextSpace = content.indexOf(' ', endIndex);
                if (nextSpace != -1 && nextSpace - endIndex < 100) {
                    endIndex = nextSpace;
                }
            }
            
            String chunk = content.substring(startIndex, endIndex);
            chunks.add(chunk);
            
            // Move to next chunk with overlap
            startIndex = endIndex - chunkOverlap;
            if (startIndex >= content.length()) {
                break;
            }
        }
        
        return chunks;
    }

    /**
     * Processes chunks using streaming approach with batching.
     */
    private List<DocumentChunk> processChunksStreaming(Document document, List<String> textChunks) {
        final int STREAM_BATCH_SIZE = 10; // Process 10 chunks at a time
        List<DocumentChunk> allChunks = new ArrayList<>();
        
        // Process chunks in streaming batches
        for (int i = 0; i < textChunks.size(); i += STREAM_BATCH_SIZE) {
            int endIndex = Math.min(i + STREAM_BATCH_SIZE, textChunks.size());
            List<String> batch = textChunks.subList(i, endIndex);
            
            log.debug("Processing streaming batch {}/{}", 
                     (i / STREAM_BATCH_SIZE) + 1, 
                     (textChunks.size() + STREAM_BATCH_SIZE - 1) / STREAM_BATCH_SIZE);
            
            // Process batch asynchronously
            CompletableFuture<List<float[]>> embeddingsFuture = 
                embeddingService.createEmbeddingsAsync(batch);
            
            try {
                List<float[]> embeddings = embeddingsFuture.get(); // Wait for completion
                
                // Create document chunks
                List<DocumentChunk> batchChunks = createDocumentChunks(
                    document, batch, embeddings, i);
                
                // Save immediately to database to free memory
                List<DocumentChunk> savedChunks = documentChunkRepository.saveAll(batchChunks);
                
                // Add minimal references to result
                allChunks.addAll(savedChunks.stream()
                    .map(chunk -> DocumentChunk.builder()
                        .id(chunk.getId())
                        .document(document)
                        .chunkIndex(chunk.getChunkIndex())
                        .build())
                    .toList());
                
                // Clear batch data to free memory
                batchChunks.clear();
                embeddings.clear();
                
                // Force garbage collection between batches
                if (endIndex < textChunks.size()) {
                    System.gc();
                    Thread.sleep(100);
                }
                
            } catch (Exception e) {
                log.error("Error processing streaming batch: {}", e.getMessage());
                // Continue with next batch instead of failing completely
            }
        }
        
        return allChunks;
    }

    /**
     * Creates document chunks from text and embeddings.
     */
    private List<DocumentChunk> createDocumentChunks(Document document, 
                                                   List<String> textChunks, 
                                                   List<float[]> embeddings, 
                                                   int startIndex) {
        return IntStream.range(0, textChunks.size())
            .mapToObj(i -> DocumentChunk.builder()
                .document(document)
                .content(textChunks.get(i))
                .chunkIndex(startIndex + i)
                .embeddingVector(embeddings.get(i))
                .build())
            .toList();
    }

    /**
     * Processes a single chunk with fallback handling.
     */
    public DocumentChunk processSingleChunk(Document document, String content, int chunkIndex) {
        try {
            float[] embedding = embeddingService.createEmbedding(content);
            
            DocumentChunk chunk = DocumentChunk.builder()
                .document(document)
                .content(content)
                .chunkIndex(chunkIndex)
                .embeddingVector(embedding)
                .build();
            
            return documentChunkRepository.save(chunk);
            
        } catch (Exception e) {
            log.error("Error processing single chunk for document {}: {}", 
                     document.getId(), e.getMessage());
            
            // Fallback: create chunk with zero embedding
            DocumentChunk fallbackChunk = DocumentChunk.builder()
                .document(document)
                .content(content)
                .chunkIndex(chunkIndex)
                .embeddingVector(new float[1536]) // Zero embedding
                .build();
            
            return documentChunkRepository.save(fallbackChunk);
        }
    }
}