package com.example.ai.api.controller;

import com.example.ai.api.dto.ChatRequest;
import com.example.ai.api.dto.ChatResponse;
import com.example.ai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * REST controller for chat operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Endpoint for chat completion.
     *
     * @param request The chat request
     * @return The chat response
     */
    @PostMapping("/complete")
    public ResponseEntity<ChatResponse> chatCompletion(@Valid @RequestBody ChatRequest request) {
        try {
            String response = chatService.generateChatResponse(request.getPrompt(), request.getSystemPrompt());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating chat response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate response", e);
        }
    }

    /**
     * Endpoint for RAG-enhanced chat completion.
     *
     * @param request The chat request
     * @return The chat response
     */
    @PostMapping("/rag")
    public ResponseEntity<ChatResponse> ragChatCompletion(@Valid @RequestBody ChatRequest request) {
        try {
            String response = chatService.generateRagResponse(request.getPrompt());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating RAG response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate RAG response", e);
        }
    }

    /**
     * Endpoint for templated chat completion.
     *
     * @param templateName The template name
     * @param variables The variables for the template
     * @return The chat response
     */
    @PostMapping("/template/{templateName}")
    public ResponseEntity<ChatResponse> templateChatCompletion(
            @PathVariable String templateName,
            @RequestBody Map<String, String> variables) {
        
        if (!variables.containsKey("prompt")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt is required");
        }
        
        try {
            String response = chatService.generateTemplatedResponse(templateName, variables);
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating templated response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate templated response", e);
        }
    }

    /**
     * Endpoint for tool-augmented chat completion.
     *
     * @param request The chat request
     * @param toolNames The names of the tools to use
     * @return The chat response
     */
    @PostMapping("/tools")
    public ResponseEntity<ChatResponse> toolChatCompletion(
            @Valid @RequestBody ChatRequest request,
            @RequestParam(required = false) List<String> toolNames) {
        
        try {
            String response = chatService.generateToolAugmentedResponse(
                    request.getPrompt(), 
                    request.getSystemPrompt(),
                    toolNames != null ? toolNames : List.of());
            
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating tool-augmented response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate tool-augmented response", e);
        }
    }
}