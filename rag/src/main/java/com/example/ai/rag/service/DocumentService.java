package com.example.ai.rag.service;

import com.example.ai.core.config.AiProperties;
import com.example.ai.core.config.AiProperties.RagConfig;
import com.example.ai.rag.model.Document;
import com.example.ai.rag.model.DocumentChunk;
import com.example.ai.rag.repository.DocumentChunkRepository;
import com.example.ai.rag.repository.DocumentRepository;
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
        
        // Split document into chunks and create embeddings
        List<DocumentChunk> chunks = splitAndEmbedDocument(document);
        documentChunkRepository.saveAll(chunks);
        
        log.info("Created document: {} with {} chunks", document.getId(), chunks.size());
        return document;
    }

    /**
     * Splits a document into chunks and creates embeddings for each chunk.
     * Uses Java 21 enhanced Stream API and pattern matching.
     *
     * @param document The document to split
     * @return List of document chunks with embeddings
     */
    private List<DocumentChunk> splitAndEmbedDocument(Document document) {
        // Using Java 21 records for immutable configuration
        RagConfig ragConfig = aiProperties.getRag().asRagConfig();
        
        String content = document.getContent();
        int chunkSize = ragConfig.chunkSize();
        int chunkOverlap = ragConfig.chunkOverlap();
        
        // Using Java 21 record for chunk parameters
        record ChunkParams(int startIndex, int endIndex, int chunkIndex) {}
        
        List<ChunkParams> chunkParams = new ArrayList<>();
        int startIndex = 0;
        int chunkIndex = 0;
        
        // Calculate chunk boundaries
        while (startIndex < content.length()) {
            int endIndex = Math.min(startIndex + chunkSize, content.length());
            
            // Adjust end index to avoid cutting words
            if (endIndex < content.length()) {
                // Find the next space after the chunk size
                int nextSpace = content.indexOf(' ', endIndex);
                if (nextSpace != -1 && nextSpace - endIndex < 50) { // Don't extend too far
                    endIndex = nextSpace;
                }
            }
            
            chunkParams.add(new ChunkParams(startIndex, endIndex, chunkIndex++));
            
            // Move to next chunk with overlap
            startIndex = endIndex - chunkOverlap;
            if (startIndex < 0 || startIndex >= content.length()) {
                break;
            }
        }
        
        // Using Java 21 enhanced Stream API to process chunks in parallel
        return chunkParams.stream()
                .map(params -> {
                    String chunkContent = content.substring(params.startIndex(), params.endIndex());
                    float[] embedding = embeddingClient.embed(chunkContent).getEmbedding();
                    
                    return DocumentChunk.builder()
                            .document(document)
                            .content(chunkContent)
                            .chunkIndex(params.chunkIndex())
                            .embeddingVector(embedding)
                            .build();
                })
                .toList();
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