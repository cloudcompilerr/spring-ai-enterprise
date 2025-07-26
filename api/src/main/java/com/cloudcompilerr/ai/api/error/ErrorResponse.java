package com.cloudcompilerr.ai.api.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response for API errors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * The HTTP status code.
     */
    private int status;
    
    /**
     * The error title.
     */
    private String title;
    
    /**
     * The error message.
     */
    private String message;
    
    /**
     * The request path.
     */
    private String path;
    
    /**
     * The timestamp of the error.
     */
    private LocalDateTime timestamp;
}