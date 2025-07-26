package com.cloudcompilerr.ai.service;

import com.cloudcompilerr.ai.core.config.AiProperties;
import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import com.cloudcompilerr.ai.prompt.model.PromptTemplate;
import com.cloudcompilerr.ai.prompt.service.PromptEngineeringService;
import com.cloudcompilerr.ai.prompt.templates.SystemPromptTemplates;
import com.cloudcompilerr.ai.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for chat-related operations.
 * This service provides methods for generating chat responses with different capabilities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final RagService ragService;
    private final PromptEngineeringService promptEngineeringService;
    private final McpService mcpService;
    private final McpToolService mcpToolService;
    private final AiProperties aiProperties;

    /**
     * Generates a chat response using the provided prompt.
     *
     * @param userPrompt The user's prompt
     * @param systemPrompt Optional system prompt to guide the AI's behavior
     * @return The AI-generated response
     */
    public String generateChatResponse(String userPrompt, String systemPrompt) {
        List<Message> messages = new ArrayList<>();
        
        // Add system message if provided
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new SystemMessage(systemPrompt));
        } else {
            // Use default system prompt if none provided
            messages.add(new SystemMessage(SystemPromptTemplates.GENERAL_PURPOSE.format()));
        }
        
        // Add user message
        messages.add(new UserMessage(userPrompt));
        
        // Create prompt and generate response
        Prompt prompt = new Prompt(messages);
        String response = chatClient.call(prompt).getResult().getOutput().getContent();
        
        log.info("Generated chat response for prompt: {}", userPrompt);
        return response;
    }

    /**
     * Generates a chat response using RAG (Retrieval-Augmented Generation).
     *
     * @param userPrompt The user's prompt
     * @return The AI-generated response
     */
    public String generateRagResponse(String userPrompt) {
        log.info("Generating RAG response for prompt: {}", userPrompt);
        return ragService.answerQuestion(userPrompt);
    }

    /**
     * Generates a chat response using a specific prompt template.
     * Uses Java 21 switch expressions for more concise code.
     *
     * @param templateName The name of the template to use
     * @param variables The variables to fill in the template
     * @return The AI-generated response
     */
    public String generateTemplatedResponse(String templateName, Map<String, String> variables) {
        log.info("Generating templated response using template: {}", templateName);
        
        // Using Java 21 switch expressions for more concise code
        PromptTemplate systemPrompt = switch (templateName) {
            case "code" -> SystemPromptTemplates.CODE_GENERATION;
            case "data" -> SystemPromptTemplates.DATA_ANALYSIS;
            case "summary" -> SystemPromptTemplates.SUMMARIZATION;
            case "qa" -> SystemPromptTemplates.QUESTION_ANSWERING;
            default -> SystemPromptTemplates.GENERAL_PURPOSE;
        };
        
        PromptTemplate userPrompt = PromptTemplate.builder()
                .template(variables.get("prompt"))
                .build();
        
        Map<String, String> systemVars = new HashMap<>(variables);
        systemVars.remove("prompt");
        
        return promptEngineeringService.executePrompt(systemPrompt, systemVars, userPrompt, new HashMap<>());
    }

    /**
     * Generates a chat response with MCP tools.
     * Uses Java 21 features like stream enhancements and pattern matching.
     *
     * @param userPrompt The user's prompt
     * @param systemPrompt Optional system prompt to guide the AI's behavior
     * @param toolNames List of tool names to make available
     * @return The AI-generated response
     */
    public String generateToolAugmentedResponse(String userPrompt, String systemPrompt, List<String> toolNames) {
        log.info("Generating tool-augmented response with tools: {}", toolNames);
        
        // Using Java 21 enhanced Stream API to filter and map in one operation
        List<McpTool> tools = toolNames.stream()
                .map(mcpToolService::getToolByName)
                .filter(tool -> tool != null)
                .toList(); // Java 21 immutable list collector
        
        // Using Java 21 pattern matching for instanceof with conditional binding
        String effectiveSystemPrompt = systemPrompt instanceof String s && !s.isEmpty() 
                ? s 
                : "You are a helpful assistant with access to tools. Use the tools when appropriate to answer the user's question.";
        
        return mcpService.executeWithTools(userPrompt, effectiveSystemPrompt, tools);
    }
}