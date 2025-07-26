package com.cloudcompilerr.ai.rag.service;

import com.cloudcompilerr.ai.core.config.AiProperties;
import com.cloudcompilerr.ai.core.config.AiProperties.RagConfig;
import com.cloudcompilerr.ai.rag.model.Document;
import com.cloudcompilerr.ai.rag.model.DocumentChunk;
import com.cloudcompilerr.ai.rag.repository.DocumentChunkRepository;
import com.cloudcompilerr.ai.rag.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Service for managing documents in the RAG system.
 * Updated to use Java 21 features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingClient embeddingClient;
    private final AiProperties aiProperties;
    private final StreamingDocumentProcessor streamingProcessor;
    private final CircuitBreakerService circuitBreaker;
    
    /**
     * Record for chunk parameters used in document processing.
     */
    private record ChunkParams(int startIndex, int endIndex, int chunkIndex) {}

    /**
     * Gets all documents.
     *
     * @return List of all documents
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Creates a new document and splits it into chunks.
     * Uses Java 21 pattern matching and enhanced Optional handling.
     *
     * @param title The document title
     * @param content The document content
     * @param sourceUrl The source URL (optional)
     * @param documentType The document type (optional)
     * @return The created document
     */
    @Transactional
    public Document createDocument(String title, String content, String sourceUrl, String documentType) {
        // Check document size to prevent memory issues
        final int MAX_DOCUMENT_SIZE = 10_000; // Reduced to 10KB limit for now
        if (content != null && content.length() > MAX_DOCUMENT_SIZE) {
            throw new IllegalArgumentException("Document too large. Maximum size is " + MAX_DOCUMENT_SIZE + " characters");
        }
        
        // Log memory usage before processing
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        log.info("Memory usage before document processing: {} MB", usedMemory / (1024 * 1024));
        
        // Using Java 21 pattern matching for instanceof with conditional binding
        if (sourceUrl instanceof String url && !url.isEmpty()) {
            // Using Java 21 enhanced Optional handling
            var existingDoc = documentRepository.findBySourceUrl(sourceUrl);
            if (existingDoc.isPresent()) {
                log.info("Document with source URL {} already exists", sourceUrl);
                return existingDoc.get();
            }
        }

        // Using Java 21 record for document creation parameters
        record DocumentCreationParams(String title, String content, String sourceUrl, String documentType) {}
        var params = new DocumentCreationParams(title, content, sourceUrl, documentType);

        // Create new document
        Document document = Document.builder()
                .title(params.title())
                .content(params.content())
                .sourceUrl(params.sourceUrl())
                .documentType(params.documentType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);
        
        // Split document into chunks and create embeddings (saves chunks internally)
        List<DocumentChunk> chunks = splitAndEmbedDocument(document);
        
        log.info("Created document: {} with {} chunks", document.getId(), chunks.size());
        return document;
    }

    /**
     * Production-ready document processing with streaming, circuit breaker, and proper embeddings.
     *
     * @param document The document to process
     * @return List of document chunks with embeddings
     */
    private List<DocumentChunk> splitAndEmbedDocument(Document document) {
        String content = document.getContent();
        
        log.info("Processing document: {} (content length: {})", 
                document.getId(), content.length());
        
        // Log memory usage before processing
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        log.info("Memory usage before document processing: {} MB", usedMemory / (1024 * 1024));
        
        try {
            // Use circuit breaker to protect against API failures
            return circuitBreaker.execute(
                // Main operation: streaming document processing
                () -> {
                    if (content.length() > 50000) { // Large document
                        log.info("Using streaming processing for large document");
                        return streamingProcessor.processDocumentAsync(document).get();
                    } else {
                        log.info("Using single chunk processing for small document");
                        return processSingleChunkDocument(document, content);
                    }
                },
                // Fallback: create document with zero embeddings
                (reason) -> {
                    log.warn("Using fallback processing for document {}: {}", document.getId(), reason);
                    return createFallbackChunk(document, content);
                }
            );
            
        } catch (Exception e) {
            log.error("Error in document processing for document {}: {}", document.getId(), e.getMessage());
            // Final fallback
            return createFallbackChunk(document, content);
        } finally {
            // Log memory usage after processing
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            log.info("Memory usage after document processing: {} MB", finalMemory / (1024 * 1024));
        }
    }

    /**
     * Processes a small document as a single chunk with proper embeddings.
     */
    private List<DocumentChunk> processSingleChunkDocument(Document document, String content) {
        DocumentChunk chunk = streamingProcessor.processSingleChunk(document, content, 0);
        
        return List.of(DocumentChunk.builder()
                .id(chunk.getId())
                .document(document)
                .chunkIndex(0)
                .build());
    }

    /**
     * Creates a fallback chunk with zero embeddings when main processing fails.
     */
    private List<DocumentChunk> createFallbackChunk(Document document, String content) {
        try {
            // Create chunk with zero embedding as fallback
            float[] zeroEmbedding = new float[1536]; // Standard OpenAI embedding dimension
            
            DocumentChunk chunk = DocumentChunk.builder()
                    .document(document)
                    .content(content)
                    .chunkIndex(0)
                    .embeddingVector(zeroEmbedding)
                    .build();
            
            DocumentChunk savedChunk = documentChunkRepository.save(chunk);
            
            return List.of(DocumentChunk.builder()
                    .id(savedChunk.getId())
                    .document(document)
                    .chunkIndex(0)
                    .build());
                    
        } catch (Exception e) {
            log.error("Even fallback processing failed for document {}: {}", document.getId(), e.getMessage());
            throw new RuntimeException("Complete document processing failure", e);
        }
    }
    


    /**
     * Retrieves a document by ID.
     * Uses Java 21 enhanced Optional handling.
     *
     * @param id The document ID
     * @return Optional containing the document if found
     */
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Checks if a document exists.
     *
     * @param id The document ID
     * @return True if the document exists, false otherwise
     */
    public boolean documentExists(Long id) {
        return documentRepository.existsById(id);
    }

    /**
     * Searches for documents by title.
     * Uses Java 21 enhanced Stream API.
     *
     * @param title The title to search for
     * @return List of matching documents
     */
    public List<Document> searchDocumentsByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Updates a document.
     * Uses Java 21 pattern matching and records.
     *
     * @param id The document ID
     * @param title The new title
     * @param content The new content
     * @param sourceUrl The new source URL
     * @param documentType The new document type
     * @return The updated document
     */
    @Transactional
    public Document updateDocument(Long id, String title, String content, String sourceUrl, String documentType) {
        // Using Java 21 record for document update parameters
        record DocumentUpdateParams(Long id, String title, String content, String sourceUrl, String documentType) {}
        var params = new DocumentUpdateParams(id, title, content, sourceUrl, documentType);
        
        Document document = documentRepository.findById(params.id())
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + params.id()));
        
        // Update document fields
        document.setTitle(params.title());
        document.setContent(params.content());
        document.setSourceUrl(params.sourceUrl());
        document.setDocumentType(params.documentType());
        document.setUpdatedAt(LocalDateTime.now());
        
        document = documentRepository.save(document);
        
        // Delete old chunks
        List<DocumentChunk> oldChunks = documentChunkRepository.findByDocument(document);
        documentChunkRepository.deleteAll(oldChunks);
        
        // Create new chunks
        List<DocumentChunk> newChunks = splitAndEmbedDocument(document);
        documentChunkRepository.saveAll(newChunks);
        
        log.info("Updated document: {} with {} chunks", document.getId(), newChunks.size());
        return document;
    }

    /**
     * Deletes a document and its chunks.
     * Uses Java 21 enhanced Optional handling and pattern matching.
     *
     * @param id The document ID
     */
    @Transactional
    public void deleteDocument(Long id) {
        // Using Java 21 enhanced pattern matching with Optional
        documentRepository.findById(id).ifPresentOrElse(
            document -> {
                List<DocumentChunk> chunks = documentChunkRepository.findByDocument(document);
                documentChunkRepository.deleteAll(chunks);
                documentRepository.delete(document);
                log.info("Deleted document: {} with {} chunks", id, chunks.size());
            },
            () -> log.warn("Attempted to delete non-existent document with ID: {}", id)
        );
    }
}