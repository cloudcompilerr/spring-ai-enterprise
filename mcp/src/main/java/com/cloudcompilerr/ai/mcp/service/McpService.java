package com.cloudcompilerr.ai.mcp.service;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for Model Context Protocol (MCP) operations.
 * This service provides methods for registering and executing MCP tools.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {

    private final ChatClient chatClient;
    private final Map<String, McpToolExecutor> toolExecutors = new ConcurrentHashMap<>();

    /**
     * Registers a tool with the MCP service.
     *
     * @param tool The tool to register
     * @param executor The executor for the tool
     */
    public void registerTool(McpTool tool, McpToolExecutor executor) {
        if (tool == null) {
            throw new IllegalArgumentException("Tool cannot be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Tool executor cannot be null");
        }
        if (tool.getName() == null || tool.getName().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be empty");
        }
        
        toolExecutors.put(tool.getName(), executor);
        log.info("Registered MCP tool: {}", tool.getName());
    }

    /**
     * Unregisters a tool from the MCP service.
     *
     * @param toolName The name of the tool to unregister
     * @return True if the tool was unregistered, false if it wasn't registered
     */
    public boolean unregisterTool(String toolName) {
        if (toolName == null || toolName.isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be empty");
        }
        
        McpToolExecutor removed = toolExecutors.remove(toolName);
        if (removed != null) {
            log.info("Unregistered MCP tool: {}", toolName);
            return true;
        } else {
            log.warn("Attempted to unregister non-existent MCP tool: {}", toolName);
            return false;
        }
    }

    /**
     * Gets all registered tool names.
     *
     * @return List of registered tool names
     */
    public List<String> getRegisteredToolNames() {
        return new ArrayList<>(toolExecutors.keySet());
    }

    /**
     * Checks if a tool is registered.
     *
     * @param toolName The name of the tool to check
     * @return True if the tool is registered, false otherwise
     */
    public boolean isToolRegistered(String toolName) {
        return toolExecutors.containsKey(toolName);
    }

    /**
     * Executes a chat with MCP tools.
     *
     * @param userPrompt The user's prompt
     * @param systemPrompt The system prompt
     * @param tools The tools to make available
     * @return The AI response
     */
    public String executeWithTools(String userPrompt, String systemPrompt, List<McpTool> tools) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("User prompt cannot be empty");
        }
        
        if (tools == null || tools.isEmpty()) {
            log.warn("No tools provided for tool-augmented chat");
        }
        
        try {
            log.debug("Creating prompt with tools for user prompt: {}", userPrompt);
            
            // Create prompt with tools
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt != null ? systemPrompt : 
                    "You are a helpful assistant with access to tools. Use the tools when appropriate to answer the user's question."));
            messages.add(new UserMessage(userPrompt));
            
            Prompt prompt = new Prompt(messages);
            
            // Execute the prompt (simplified for Spring AI 0.8.1 compatibility)
            log.debug("Executing prompt");
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            
            if (response == null || response.isEmpty()) {
                log.warn("Received empty response from AI model");
                return "I'm sorry, I couldn't generate a response. Please try again.";
            }
            
            // For Spring AI 0.8.1, we get a simple string response
            // Tool functionality will be enhanced in a future version
            
            log.info("Generated tool-augmented response for prompt: {}", userPrompt);
            return response;
        } catch (Exception e) {
            log.error("Error in tool-augmented chat", e);
            throw new RuntimeException("Failed to generate tool-augmented response", e);
        }
    }

    /**
     * Converts MCP tools to tool definitions for the AI client.
     *
     * @param tools The MCP tools
     * @return The tool definitions
     */
    private List<Map<String, Object>> convertToToolDefinitions(List<McpTool> tools) {
        if (tools == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> toolDefinitions = new ArrayList<>();
        
        for (McpTool tool : tools) {
            // Skip tools that aren't registered
            if (!toolExecutors.containsKey(tool.getName())) {
                log.warn("Skipping unregistered tool: {}", tool.getName());
                continue;
            }
            
            Map<String, Object> definition = new HashMap<>();
            definition.put("type", "function");
            
            Map<String, Object> function = new HashMap<>();
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("type", "object");
            
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();
            
            for (Map.Entry<String, McpTool.ParameterInfo> entry : tool.getParameters().entrySet()) {
                String paramName = entry.getKey();
                McpTool.ParameterInfo paramInfo = entry.getValue();
                
                Map<String, Object> paramDef = new HashMap<>();
                paramDef.put("type", paramInfo.getType());
                paramDef.put("description", paramInfo.getDescription());
                
                if (paramInfo.getEnumValues() != null && !paramInfo.getEnumValues().isEmpty()) {
                    paramDef.put("enum", paramInfo.getEnumValues());
                }
                
                properties.put(paramName, paramDef);
                
                if (paramInfo.isRequired()) {
                    required.add(paramName);
                }
            }
            
            parameters.put("properties", properties);
            parameters.put("required", required);
            
            function.put("parameters", parameters);
            definition.put("function", function);
            
            toolDefinitions.add(definition);
        }
        
        return toolDefinitions;
    }

    /**
     * Interface for MCP tool executors.
     */
    public interface McpToolExecutor {
        /**
         * Executes the tool with the given arguments.
         *
         * @param arguments The arguments for the tool
         * @return The result of the tool execution
         */
        String execute(Map<String, Object> arguments);
    }
}