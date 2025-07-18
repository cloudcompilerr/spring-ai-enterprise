package com.example.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for document creation requests.
 * Using Java 21 record for immutable data transfer.
 */
public record DocumentRequest(
    /**
     * The document title.
     */
    @NotBlank(message = "Title cannot be empty")
    String title,
    
    /**
     * The document content.
     */
    @NotBlank(message = "Content cannot be empty")
    String content,
    
    /**
     * The source URL of the document (optional).
     */
    String sourceUrl,
    
    /**
     * The type of document (optional).
     */
    String documentType
) {
    /**
     * Validates that the document request is valid.
     * Uses Java 21 pattern matching for instanceof.
     * 
     * @return True if the request is valid
     */
    public boolean isValid() {
        return title instanceof String t && !t.isBlank() &&
               content instanceof String c && !c.isBlank();
    }
    
    /**
     * Returns a summary of the document request.
     * Uses Java 21 text blocks for better readability.
     * 
     * @return A summary of the document request
     */
    public String getSummary() {
        return """
               Document Request:
               - Title: %s
               - Content Length: %d characters
               - Source URL: %s
               - Document Type: %s
               """.formatted(
                   title,
                   content != null ? content.length() : 0,
                   sourceUrl != null ? sourceUrl : "N/A",
                   documentType != null ? documentType : "Unknown"
               );
    }
    
    /**
     * Creates a new DocumentRequest with a different title.
     * 
     * @param newTitle The new title
     * @return A new DocumentRequest with the updated title
     */
    public DocumentRequest withTitle(String newTitle) {
        return new DocumentRequest(newTitle, content, sourceUrl, documentType);
    }
    
    /**
     * Creates a new DocumentRequest with a different content.
     * 
     * @param newContent The new content
     * @return A new DocumentRequest with the updated content
     */
    public DocumentRequest withContent(String newContent) {
        return new DocumentRequest(title, newContent, sourceUrl, documentType);
    }
}