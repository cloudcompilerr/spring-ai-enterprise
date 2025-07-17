package com.example.ai.prompt.service;

import com.example.ai.core.config.AiProperties;
import com.example.ai.prompt.model.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for handling prompt engineering operations.
 * This service provides methods for creating and executing prompts with different templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptEngineeringService {

    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    /**
     * Executes a prompt with a system message and a user message.
     *
     * @param systemPrompt The system prompt template
     * @param systemPromptVars Variables for the system prompt
     * @param userPrompt The user prompt template
     * @param userPromptVars Variables for the user prompt
     * @return The AI response
     */
    public String executePrompt(
            PromptTemplate systemPrompt, Map<String, String> systemPromptVars,
            PromptTemplate userPrompt, Map<String, String> userPromptVars) {
        
        String formattedSystemPrompt = systemPrompt.format(systemPromptVars);
        String formattedUserPrompt = userPrompt.format(userPromptVars);
        
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(formattedSystemPrompt));
        messages.add(new UserMessage(formattedUserPrompt));
        
        Prompt prompt = new Prompt(messages);
        
        log.debug("Executing prompt: {}", prompt);
        ChatResponse response = chatClient.call(prompt);
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * Executes a prompt with just a user message and default system behavior.
     *
     * @param userPrompt The user prompt template
     * @param userPromptVars Variables for the user prompt
     * @return The AI response
     */
    public String executePrompt(PromptTemplate userPrompt, Map<String, String> userPromptVars) {
        String formattedUserPrompt = userPrompt.format(userPromptVars);
        
        Prompt prompt = PromptBuilder.builder()
                .user(formattedUserPrompt)
                .build();
        
        log.debug("Executing prompt: {}", prompt);
        ChatResponse response = chatClient.call(prompt);
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * Executes a chain of prompts, where the output of each prompt is used as input for the next.
     *
     * @param promptChain List of prompt templates to execute in sequence
     * @param initialVars Initial variables for the first prompt
     * @return The final AI response
     */
    public String executePromptChain(List<PromptTemplate> promptChain, Map<String, String> initialVars) {
        String currentInput = "";
        Map<String, String> currentVars = initialVars;
        
        for (PromptTemplate template : promptChain) {
            // Add the previous output as a variable
            if (!currentInput.isEmpty()) {
                currentVars.put("previous_output", currentInput);
            }
            
            // Execute the current prompt
            currentInput = executePrompt(template, currentVars);
        }
        
        return currentInput;
    }
}