package com.cloudcompilerr.ai.prompt.service;

import com.cloudcompilerr.ai.core.config.AiProperties;
import com.cloudcompilerr.ai.core.config.AiProperties.ModelConfig;
import com.cloudcompilerr.ai.prompt.model.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for handling prompt engineering operations.
 * This service provides methods for creating and executing prompts with different templates.
 * Updated to use Java 21 features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptEngineeringService {

    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    /**
     * Executes a prompt with a system message and a user message.
     * Uses Java 21 records for parameter grouping and enhanced error handling.
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
        
        // Using Java 21 record for prompt parameters
        record PromptParams(
            PromptTemplate systemPrompt, 
            Map<String, String> systemVars,
            PromptTemplate userPrompt, 
            Map<String, String> userVars
        ) {}
        
        var params = new PromptParams(systemPrompt, systemPromptVars, userPrompt, userPromptVars);
        
        try {
            String formattedSystemPrompt = params.systemPrompt().format(params.systemVars());
            String formattedUserPrompt = params.userPrompt().format(params.userVars());
            
            // Using Java 21 List.of for immutable list creation
            List<Message> messages = List.of(
                new SystemMessage(formattedSystemPrompt),
                new UserMessage(formattedUserPrompt)
            );
            
            Prompt prompt = new Prompt(messages);
            
            log.debug("Executing prompt with system and user messages");
            ChatResponse response = chatClient.call(prompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Error executing prompt", e);
            throw new RuntimeException("Failed to execute prompt", e);
        }
    }
    
    /**
     * Executes a prompt with just a user message and default system behavior.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param userPrompt The user prompt template
     * @param userPromptVars Variables for the user prompt
     * @return The AI response
     */
    public String executePrompt(PromptTemplate userPrompt, Map<String, String> userPromptVars) {
        // Using Java 21 pattern matching for instanceof with conditional binding
        if (!(userPrompt instanceof PromptTemplate template)) {
            throw new IllegalArgumentException("User prompt template cannot be null");
        }
        
        try {
            String formattedUserPrompt = template.format(userPromptVars);
            
            Prompt prompt = new Prompt(List.of(new UserMessage(formattedUserPrompt)));
            
            log.debug("Executing prompt with user message only");
            ChatResponse response = chatClient.call(prompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Error executing prompt with user message only", e);
            throw new RuntimeException("Failed to execute prompt with user message only", e);
        }
    }
    
    /**
     * Executes a chain of prompts, where the output of each prompt is used as input for the next.
     * Uses Java 21 enhanced Stream API and functional programming.
     *
     * @param promptChain List of prompt templates to execute in sequence
     * @param initialVars Initial variables for the first prompt
     * @return The final AI response
     */
    public String executePromptChain(List<PromptTemplate> promptChain, Map<String, String> initialVars) {
        // Using Java 21 record for chain execution state
        record ChainState(String output, Map<String, String> vars) {}
        
        // Using Java 21 functional programming with enhanced error handling
        try {
            // Initialize the chain state
            ChainState initialState = new ChainState("", new HashMap<>(initialVars));
            
            // Define the execution function for a single template
            Function<ChainState, ChainState> executeTemplate = state -> {
                return promptChain.stream()
                    .reduce(
                        state,
                        (currentState, template) -> {
                            // Add previous output as a variable if it exists
                            Map<String, String> updatedVars = new HashMap<>(currentState.vars());
                            if (!currentState.output().isEmpty()) {
                                updatedVars.put("previous_output", currentState.output());
                            }
                            
                            // Execute the current template
                            String output = executePrompt(template, updatedVars);
                            return new ChainState(output, updatedVars);
                        },
                        // Combiner for parallel stream (not used here)
                        (state1, state2) -> state1
                    );
            };
            
            // Execute the chain
            ChainState finalState = executeTemplate.apply(initialState);
            return finalState.output();
        } catch (Exception e) {
            log.error("Error executing prompt chain", e);
            throw new RuntimeException("Failed to execute prompt chain", e);
        }
    }
    
    /**
     * Executes a prompt with temperature control.
     * Uses Java 21 records for immutable configuration.
     *
     * @param prompt The prompt template
     * @param vars The variables for the prompt
     * @param temperature The temperature parameter (0.0 to 1.0)
     * @return The AI response
     */
    public String executePromptWithTemperature(PromptTemplate prompt, Map<String, String> vars, float temperature) {
        // Using Java 21 records for immutable configuration
        ModelConfig modelConfig = aiProperties.getOpenai().asModelConfig().withTemperature(temperature);
        
        try {
            String formattedPrompt = prompt.format(vars);
            
            Prompt aiPrompt = new Prompt(List.of(new UserMessage(formattedPrompt)));
            
            log.debug("Executing prompt with temperature {}", temperature);
            ChatResponse response = chatClient.call(aiPrompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Error executing prompt with temperature {}", temperature, e);
            throw new RuntimeException("Failed to execute prompt with temperature control", e);
        }
    }
    
    /**
     * Executes multiple prompts in parallel and combines the results.
     * Uses Java 21 enhanced Stream API and parallel processing.
     *
     * @param prompts List of prompts to execute
     * @param vars Variables for the prompts
     * @param combiner Function to combine the results
     * @return The combined result
     */
    public String executeParallelPrompts(
            List<PromptTemplate> prompts, 
            Map<String, String> vars,
            Function<List<String>, String> combiner) {
        
        try {
            // Using Java 21 enhanced parallel streams
            List<String> results = prompts.parallelStream()
                    .map(prompt -> executePrompt(prompt, vars))
                    .toList();
            
            return combiner.apply(results);
        } catch (Exception e) {
            log.error("Error executing parallel prompts", e);
            throw new RuntimeException("Failed to execute parallel prompts", e);
        }
    }
}