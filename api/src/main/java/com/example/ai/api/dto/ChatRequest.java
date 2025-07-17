package com.example.ai.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * The user's prompt.
     */
    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;
    
    /**
     * Optional system prompt to guide the AI's behavior.
     */
    private String systemPrompt;
}