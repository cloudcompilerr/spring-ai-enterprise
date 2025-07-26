package com.cloudcompilerr.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for chat requests.
 * Using Java 21 record for immutable data transfer.
 */
public record ChatRequest(
    /**
     * The user's prompt.
     */
    @NotBlank(message = "Prompt cannot be empty")
    String prompt,
    
    /**
     * Optional system prompt to guide the AI's behavior.
     */
    String systemPrompt
) {
    /**
     * Validates that the chat request is valid.
     * Uses Java 21 pattern matching for instanceof.
     * 
     * @return True if the request is valid
     */
    public boolean isValid() {
        return prompt instanceof String p && !p.isBlank();
    }
    
    /**
     * Returns a summary of the chat request.
     * Uses Java 21 text blocks for better readability.
     * 
     * @return A summary of the chat request
     */
    public String getSummary() {
        return """
               Chat Request:
               - Prompt: %s
               - System Prompt: %s
               """.formatted(
                   prompt,
                   systemPrompt != null ? systemPrompt : "None"
               );
    }
    
    /**
     * Creates a new ChatRequest with a different prompt.
     * 
     * @param newPrompt The new prompt
     * @return A new ChatRequest with the updated prompt
     */
    public ChatRequest withPrompt(String newPrompt) {
        return new ChatRequest(newPrompt, systemPrompt);
    }
    
    /**
     * Creates a new ChatRequest with a different system prompt.
     * 
     * @param newSystemPrompt The new system prompt
     * @return A new ChatRequest with the updated system prompt
     */
    public ChatRequest withSystemPrompt(String newSystemPrompt) {
        return new ChatRequest(prompt, newSystemPrompt);
    }
}