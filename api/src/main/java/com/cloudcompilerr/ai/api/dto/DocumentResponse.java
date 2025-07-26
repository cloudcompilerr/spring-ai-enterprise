package com.cloudcompilerr.ai.api.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * DTO for document responses.
 * Using Java 21 record for immutable data transfer.
 */
public record DocumentResponse(
    Long id,
    String title,
    String content,
    String sourceUrl,
    String documentType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Returns a summary of the document.
     * Uses Java 21 text blocks for better readability.
     * 
     * @return A summary of the document
     */
    public String getSummary() {
        return """
               Document: %s (ID: %d)
               Type: %s
               Created: %s
               Content Length: %d characters
               """.formatted(
                   title,
                   id,
                   documentType != null ? documentType : "Unknown",
                   createdAt,
                   content != null ? content.length() : 0
               );
    }
    
    /**
     * Returns a truncated version of the content.
     * 
     * @param maxLength The maximum length
     * @return The truncated content
     */
    public String getTruncatedContent(int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * Returns the age of the document in days.
     * 
     * @return The age in days
     */
    public long getAgeInDays() {
        return ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }
    
    /**
     * Returns whether the document is recent (less than 30 days old).
     * 
     * @return True if the document is recent
     */
    public boolean isRecent() {
        return getAgeInDays() < 30;
    }
}