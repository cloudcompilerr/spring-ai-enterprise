package com.cloudcompilerr.ai.rag.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Entity representing a document in the RAG system.
 * This class stores the document content and metadata.
 * Updated to use Java 21 features.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "metadata")
    private String metadata;
    
    /**
     * Creates a document summary using Java 21 text blocks.
     * 
     * @return A summary of the document
     */
    public String getSummary() {
        return """
               Document: %s (ID: %d)
               Type: %s
               Source: %s
               Created: %s
               Updated: %s
               Content Length: %d characters
               """.formatted(
                   title,
                   id,
                   documentType != null ? documentType : "Unknown",
                   sourceUrl != null ? sourceUrl : "N/A",
                   createdAt,
                   updatedAt,
                   content != null ? content.length() : 0
               );
    }
    
    /**
     * Creates a document info record using Java 21 records.
     * 
     * @return An immutable record with document information
     */
    public DocumentInfo toDocumentInfo() {
        return new DocumentInfo(
            id,
            title,
            sourceUrl,
            documentType,
            createdAt,
            updatedAt,
            content != null ? content.length() : 0
        );
    }
    
    /**
     * Immutable record for document information using Java 21 records.
     */
    public record DocumentInfo(
        Long id,
        String title,
        String sourceUrl,
        String documentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int contentLength
    ) {
        /**
         * Gets the age of the document in days.
         * 
         * @return The age in days
         */
        public long getAgeInDays() {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        }
        
        /**
         * Checks if the document is recent (less than 30 days old).
         * 
         * @return True if the document is recent
         */
        public boolean isRecent() {
            return getAgeInDays() < 30;
        }
    }
    
    /**
     * Gets the document type with a default value if null.
     * Uses Java 21 pattern matching for instanceof.
     * 
     * @return The document type or a default value
     */
    public String getDocumentTypeOrDefault() {
        return documentType instanceof String type ? type : "Unknown";
    }
    
    /**
     * Gets the source URL as an Optional.
     * Uses Java 21 enhanced Optional handling.
     * 
     * @return Optional containing the source URL if present
     */
    public Optional<String> getSourceUrlOptional() {
        return Optional.ofNullable(sourceUrl);
    }
}