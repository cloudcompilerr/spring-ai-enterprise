package com.cloudcompilerr.ai.rag.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Entity representing a chunk of a document in the RAG system.
 * Documents are split into chunks for more effective retrieval.
 * Updated to use Java 21 features.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(name = "embedding_vector", columnDefinition = "vector(1536)")
    private float[] embeddingVector;
    
    /**
     * Creates a chunk summary using Java 21 text blocks.
     * 
     * @return A summary of the chunk
     */
    public String getSummary() {
        return """
               Chunk: %d (Document: %s, ID: %d)
               Index: %d
               Content Length: %d characters
               Embedding Dimensions: %d
               """.formatted(
                   id,
                   document != null ? document.getTitle() : "Unknown",
                   document != null ? document.getId() : 0,
                   chunkIndex != null ? chunkIndex : 0,
                   content != null ? content.length() : 0,
                   embeddingVector != null ? embeddingVector.length : 0
               );
    }
    
    /**
     * Creates a chunk info record using Java 21 records.
     * 
     * @return An immutable record with chunk information
     */
    public ChunkInfo toChunkInfo() {
        return new ChunkInfo(
            id,
            document != null ? document.getId() : null,
            document != null ? document.getTitle() : null,
            chunkIndex,
            content != null ? content.length() : 0,
            embeddingVector != null ? embeddingVector.length : 0
        );
    }
    
    /**
     * Immutable record for chunk information using Java 21 records.
     */
    public record ChunkInfo(
        Long id,
        Long documentId,
        String documentTitle,
        Integer chunkIndex,
        int contentLength,
        int embeddingDimensions
    ) {
        /**
         * Creates a formatted string representation of the chunk info.
         * Uses Java 21 text blocks.
         * 
         * @return A formatted string representation
         */
        @Override
        public String toString() {
            return """
                   ChunkInfo[id=%d, documentId=%d, documentTitle=%s, chunkIndex=%d, contentLength=%d, embeddingDimensions=%d]
                   """.formatted(
                       id,
                       documentId,
                       documentTitle,
                       chunkIndex,
                       contentLength,
                       embeddingDimensions
                   ).trim();
        }
    }
    
    /**
     * Calculates the L2 norm (magnitude) of the embedding vector.
     * Uses Java 21 enhanced Stream API.
     * 
     * @return The L2 norm of the embedding vector
     */
    public double getEmbeddingNorm() {
        if (embeddingVector == null || embeddingVector.length == 0) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (float value : embeddingVector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Calculates the cosine similarity between this chunk's embedding and another vector.
     * Uses Java 21 enhanced Stream API.
     * 
     * @param otherVector The other vector to compare with
     * @return The cosine similarity, or 0 if either vector is null or empty
     */
    public double cosineSimilarity(float[] otherVector) {
        if (embeddingVector == null || otherVector == null || 
            embeddingVector.length == 0 || otherVector.length == 0 ||
            embeddingVector.length != otherVector.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        for (int i = 0; i < embeddingVector.length; i++) {
            dotProduct += embeddingVector[i] * otherVector[i];
        }
        
        double norm1 = getEmbeddingNorm();
        double sum2 = 0.0;
        for (float value : otherVector) {
            sum2 += value * value;
        }
        double norm2 = Math.sqrt(sum2);
        
        return dotProduct / (norm1 * norm2);
    }
    
    /**
     * Gets the document title safely.
     * Uses Java 21 pattern matching for instanceof.
     * 
     * @return The document title or a default value
     */
    public String getDocumentTitleSafe() {
        return document instanceof Document doc ? doc.getTitle() : "Unknown";
    }
    
    /**
     * Gets the document as an Optional.
     * Uses Java 21 enhanced Optional handling.
     * 
     * @return Optional containing the document if present
     */
    public Optional<Document> getDocumentOptional() {
        return Optional.ofNullable(document);
    }
}