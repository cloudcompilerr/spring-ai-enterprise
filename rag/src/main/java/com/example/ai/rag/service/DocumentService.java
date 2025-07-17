package com.example.ai.rag.service;

import com.example.ai.core.config.AiProperties;
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

/**
 * Service for managing documents in the RAG system.
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
     * Creates a new document and splits it into chunks.
     *
     * @param title The document title
     * @param content The document content
     * @param sourceUrl The source URL (optional)
     * @param documentType The document type (optional)
     * @return The created document
     */
    @Transactional
    public Document createDocument(String title, String content, String sourceUrl, String documentType) {
        // Check if document with the same source URL already exists
        if (sourceUrl != null && !sourceUrl.isEmpty()) {
            Optional<Document> existingDoc = documentRepository.findBySourceUrl(sourceUrl);
            if (existingDoc.isPresent()) {
                log.info("Document with source URL {} already exists", sourceUrl);
                return existingDoc.get();
            }
        }

        // Create new document
        Document document = Document.builder()
                .title(title)
                .content(content)
                .sourceUrl(sourceUrl)
                .documentType(documentType)
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
     *
     * @param document The document to split
     * @return List of document chunks with embeddings
     */
    private List<DocumentChunk> splitAndEmbedDocument(Document document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String content = document.getContent();
        
        int chunkSize = aiProperties.getRag().getChunkSize();
        int chunkOverlap = aiProperties.getRag().getChunkOverlap();
        
        // Simple text splitting strategy
        int startIndex = 0;
        int chunkIndex = 0;
        
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
            
            String chunkContent = content.substring(startIndex, endIndex);
            
            // Create embedding for the chunk
            float[] embedding = embeddingClient.embed(chunkContent).getEmbedding();
            
            // Create and save chunk
            DocumentChunk chunk = DocumentChunk.builder()
                    .document(document)
                    .content(chunkContent)
                    .chunkIndex(chunkIndex++)
                    .embeddingVector(embedding)
                    .build();
            
            chunks.add(chunk);
            
            // Move to next chunk with overlap
            startIndex = endIndex - chunkOverlap;
            if (startIndex < 0 || startIndex >= content.length()) {
                break;
            }
        }
        
        return chunks;
    }

    /**
     * Retrieves a document by ID.
     *
     * @param id The document ID
     * @return Optional containing the document if found
     */
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Searches for documents by title.
     *
     * @param title The title to search for
     * @return List of matching documents
     */
    public List<Document> searchDocumentsByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Deletes a document and its chunks.
     *
     * @param id The document ID
     */
    @Transactional
    public void deleteDocument(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        if (document.isPresent()) {
            List<DocumentChunk> chunks = documentChunkRepository.findByDocument(document.get());
            documentChunkRepository.deleteAll(chunks);
            documentRepository.delete(document.get());
            log.info("Deleted document: {} with {} chunks", id, chunks.size());
        }
    }
}