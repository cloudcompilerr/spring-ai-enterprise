package com.example.ai.service;

import com.example.ai.core.config.AiProperties;
import com.example.ai.prompt.model.PromptTemplate;
import com.example.ai.prompt.service.PromptEngineeringService;
import com.example.ai.rag.service.RagService;
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
 * This service provides methods for generating chat responses with and without RAG.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final RagService ragService;
    private final PromptEngineeringService promptEngineeringService;
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
        return ragService.answerQuestion(userPrompt);
    }

    /**
     * Generates a chat response using a specific prompt template.
     *
     * @param templateName The name of the template to use
     * @param variables The variables to fill in the template
     * @return The AI-generated response
     */
    public String generateTemplatedResponse(String templateName, Map<String, String> variables) {
        PromptTemplate userPrompt = PromptTemplate.builder()
                .template(variables.get("prompt"))
                .build();
        
        Map<String, String> promptVars = new HashMap<>(variables);
        promptVars.remove("prompt");
        
        return promptEngineeringService.executePrompt(userPrompt, promptVars);
    }
}