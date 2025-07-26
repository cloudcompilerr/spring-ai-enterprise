package com.cloudcompilerr.ai.api.controller;

import com.cloudcompilerr.ai.api.dto.ChatRequest;
import com.cloudcompilerr.ai.api.dto.ChatResponse;
import com.cloudcompilerr.ai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * REST controller for chat operations.
 * Updated to use Java 21 features.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Endpoint for chat completion.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param request The chat request
     * @return The chat response
     */
    @PostMapping("/complete")
    public ResponseEntity<ChatResponse> chatCompletion(@Valid @RequestBody ChatRequest request) {
        try {
            // Using Java 21 record pattern matching
            String response = chatService.generateChatResponse(request.prompt(), request.systemPrompt());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating chat response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate response", e);
        }
    }

    /**
     * Endpoint for RAG-enhanced chat completion.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param request The chat request
     * @return The chat response
     */
    @PostMapping("/rag")
    public ResponseEntity<ChatResponse> ragChatCompletion(@Valid @RequestBody ChatRequest request) {
        try {
            // Using Java 21 record pattern matching
            String response = chatService.generateRagResponse(request.prompt());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating RAG response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate RAG response", e);
        }
    }

    /**
     * Endpoint for templated chat completion.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param templateName The template name
     * @param variables The variables for the template
     * @return The chat response
     */
    @PostMapping("/template/{templateName}")
    public ResponseEntity<ChatResponse> templateChatCompletion(
            @PathVariable String templateName,
            @RequestBody Map<String, String> variables) {
        
        // Using Java 21 pattern matching in switch expression
        return switch (variables) {
            case Map<String, String> vars when !vars.containsKey("prompt") ->
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt is required");
                
            case Map<String, String> vars -> {
                try {
                    String response = chatService.generateTemplatedResponse(templateName, vars);
                    yield ResponseEntity.ok(new ChatResponse(response));
                } catch (Exception e) {
                    log.error("Error generating templated response", e);
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Failed to generate templated response", 
                            e
                    );
                }
            }
        };
    }

    /**
     * Endpoint for tool-augmented chat completion.
     * Uses Java 21 functional programming and enhanced error handling.
     *
     * @param request The chat request
     * @param toolNames The names of the tools to use
     * @return The chat response
     */
    @PostMapping("/tools")
    public ResponseEntity<ChatResponse> toolChatCompletion(
            @Valid @RequestBody ChatRequest request,
            @RequestParam(required = false) List<String> toolNames) {
        
        // Using Java 21 record for tool parameters
        record ToolParams(ChatRequest request, List<String> toolNames) {}
        var params = new ToolParams(request, toolNames != null ? toolNames : List.of());
        
        try {
            // Using Java 21 functional programming
            Function<ToolParams, String> generateResponse = p -> 
                chatService.generateToolAugmentedResponse(
                    p.request().prompt(), 
                    p.request().systemPrompt(),
                    p.toolNames()
                );
            
            String response = generateResponse.apply(params);
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error generating tool-augmented response", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to generate tool-augmented response", 
                    e
            );
        }
    }
    
    /**
     * Endpoint for batch chat completion.
     * Uses Java 21 enhanced Stream API and parallel processing.
     *
     * @param requests The list of chat requests
     * @return The list of chat responses
     */
    @PostMapping("/batch")
    public ResponseEntity<List<ChatResponse>> batchChatCompletion(
            @Valid @RequestBody List<ChatRequest> requests) {
        
        try {
            // Using Java 21 enhanced parallel streams
            List<ChatResponse> responses = requests.parallelStream()
                    .map(request -> {
                        try {
                            String response = chatService.generateChatResponse(
                                    request.prompt(), 
                                    request.systemPrompt()
                            );
                            return new ChatResponse(response);
                        } catch (Exception e) {
                            log.error("Error processing batch request", e);
                            return new ChatResponse("Error: " + e.getMessage());
                        }
                    })
                    .toList();
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error processing batch requests", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to process batch requests", 
                    e
            );
        }
    }
}