package com.example.ai.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for document creation requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    /**
     * The document title.
     */
    @NotBlank(message = "Title cannot be empty")
    private String title;
    
    /**
     * The document content.
     */
    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    /**
     * The source URL of the document (optional).
     */
    private String sourceUrl;
    
    /**
     * The type of document (optional).
     */
    private String documentType;
}