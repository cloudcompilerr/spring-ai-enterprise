package com.example.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for document responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    /**
     * The document ID.
     */
    private Long id;
    
    /**
     * The document title.
     */
    private String title;
    
    /**
     * The document content.
     */
    private String content;
    
    /**
     * The source URL of the document.
     */
    private String sourceUrl;
    
    /**
     * The type of document.
     */
    private String documentType;
    
    /**
     * The creation timestamp.
     */
    private LocalDateTime createdAt;
    
    /**
     * The last update timestamp.
     */
    private LocalDateTime updatedAt;
}