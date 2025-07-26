package com.cloudcompilerr.ai.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for chat responses.
 * Using Java 21 record for immutable data transfer.
 */
public record ChatResponse(
    /**
     * The AI-generated response.
     */
    String response,
    
    /**
     * The timestamp when the response was generated.
     */
    LocalDateTime timestamp
) {
    /**
     * Constructor with just the response.
     * Automatically sets the timestamp to now.
     * 
     * @param response The AI-generated response
     */
    public ChatResponse(String response) {
        this(response, LocalDateTime.now());
    }
    
    /**
     * Returns a summary of the chat response.
     * Uses Java 21 text blocks for better readability.
     * 
     * @return A summary of the chat response
     */
    public String getSummary() {
        return """
               Chat Response:
               - Generated at: %s
               - Response length: %d characters
               """.formatted(
                   timestamp,
                   response != null ? response.length() : 0
               );
    }
    
    /**
     * Returns a truncated version of the response.
     * 
     * @param maxLength The maximum length
     * @return The truncated response
     */
    public String getTruncatedResponse(int maxLength) {
        if (response == null || response.length() <= maxLength) {
            return response;
        }
        return response.substring(0, maxLength) + "...";
    }
}