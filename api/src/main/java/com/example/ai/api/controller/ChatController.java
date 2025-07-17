package com.example.ai.api.controller;

import com.example.ai.api.dto.ChatRequest;
import com.example.ai.api.dto.ChatResponse;
import com.example.ai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for chat operations.
 */
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
        String response = chatService.generateChatResponse(request.getPrompt(), request.getSystemPrompt());
        return ResponseEntity.ok(new ChatResponse(response));
    }

    /**
     * Endpoint for RAG-enhanced chat completion.
     *
     * @param request The chat request
     * @return The chat response
     */
    @PostMapping("/rag")
    public ResponseEntity<ChatResponse> ragChatCompletion(@Valid @RequestBody ChatRequest request) {
        String response = chatService.generateRagResponse(request.getPrompt());
        return ResponseEntity.ok(new ChatResponse(response));
    }
}