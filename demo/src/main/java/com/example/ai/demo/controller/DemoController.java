package com.example.ai.demo.controller;

import com.example.ai.api.dto.ChatRequest;
import com.example.ai.api.dto.ChatResponse;
import com.example.ai.api.dto.DocumentRequest;
import com.example.ai.api.dto.DocumentResponse;
import com.example.ai.mcp.model.McpTool;
import com.example.ai.service.ChatService;
import com.example.ai.service.McpToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for the demo web interface.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DemoController {

    private final ChatService chatService;
    private final McpToolService mcpToolService;
    private final RestTemplate restTemplate;

    /**
     * Home page.
     *
     * @param model The model
     * @return The home view
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Spring AI Showcase");
        return "index";
    }

    /**
     * Chat demo page.
     *
     * @param model The model
     * @return The chat view
     */
    @GetMapping("/chat")
    public String chat(Model model) {
        model.addAttribute("title", "Chat Demo");
        return "chat";
    }

    /**
     * RAG demo page.
     *
     * @param model The model
     * @return The RAG view
     */
    @GetMapping("/rag")
    public String rag(Model model) {
        model.addAttribute("title", "RAG Demo");
        return "rag";
    }

    /**
     * Document management page.
     *
     * @param model The model
     * @return The documents view
     */
    @GetMapping("/documents")
    public String documents(Model model) {
        model.addAttribute("title", "Document Management");
        return "documents";
    }

    /**
     * Process a chat request.
     *
     * @param prompt The user's prompt
     * @param systemPrompt The system prompt
     * @return The chat response
     */
    @PostMapping("/process-chat")
    public ResponseEntity<ChatResponse> processChat(
            @RequestParam String prompt,
            @RequestParam(required = false) String systemPrompt) {
        
        try {
            log.info("Processing chat request: {}", prompt);
            ChatRequest request = new ChatRequest(prompt, systemPrompt);
            String response = chatService.generateChatResponse(request.prompt(), request.systemPrompt());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process chat request", e);
        }
    }

    /**
     * Process a RAG request.
     *
     * @param prompt The user's prompt
     * @return The RAG response
     */
    @PostMapping("/process-rag")
    public ResponseEntity<ChatResponse> processRag(@RequestParam String prompt) {
        try {
            log.info("Processing RAG request: {}", prompt);
            ChatRequest request = new ChatRequest(prompt, null);
            String response = chatService.generateRagResponse(request.prompt());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error processing RAG request", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process RAG request", e);
        }
    }

    /**
     * Process a tool-augmented chat request.
     *
     * @param prompt The user's prompt
     * @param systemPrompt The system prompt
     * @param toolNames The names of the tools to use
     * @return The tool-augmented chat response
     */
    @PostMapping("/process-tool-chat")
    public ResponseEntity<ChatResponse> processToolChat(
            @RequestParam String prompt,
            @RequestParam(required = false) String systemPrompt,
            @RequestParam(required = false) List<String> toolNames) {
        
        try {
            log.info("Processing tool-augmented chat request: {}", prompt);
            ChatRequest request = new ChatRequest(prompt, systemPrompt);
            String response = chatService.generateToolAugmentedResponse(
                    request.prompt(), 
                    request.systemPrompt(),
                    toolNames != null ? toolNames : List.of());
            
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Error processing tool-augmented chat request", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process tool-augmented chat request", e);
        }
    }

    /**
     * Upload a document for RAG.
     *
     * @param title The document title
     * @param content The document content
     * @param sourceUrl The source URL
     * @param documentType The document type
     * @return The document response
     */
    @PostMapping("/upload-document")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String sourceUrl,
            @RequestParam(required = false) String documentType) {
        
        try {
            log.info("Uploading document: {}", title);
            DocumentRequest request = new DocumentRequest(title, content, sourceUrl, documentType);
            ResponseEntity<DocumentResponse> response = restTemplate.postForEntity(
                    "http://localhost:8080/api/documents",
                    request,
                    DocumentResponse.class
            );
            return response;
        } catch (Exception e) {
            log.error("Error uploading document", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload document", e);
        }
    }
    
    /**
     * Get available tools.
     *
     * @return List of available tools
     */
    @GetMapping("/api/tools")
    @ResponseBody
    public ResponseEntity<List<McpTool>> getAvailableTools() {
        try {
            List<McpTool> tools = mcpToolService.getAvailableTools();
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            log.error("Error retrieving available tools", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve available tools", e);
        }
    }
}