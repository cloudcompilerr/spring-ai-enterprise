package com.example.ai.mcp.service;

import com.example.ai.mcp.model.McpTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptBuilder;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Model Context Protocol (MCP) operations.
 * This service provides methods for registering and executing MCP tools.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {

    private final OpenAiChatClient chatClient;
    private final Map<String, McpToolExecutor> toolExecutors = new HashMap<>();

    /**
     * Registers a tool with the MCP service.
     *
     * @param tool The tool to register
     * @param executor The executor for the tool
     */
    public void registerTool(McpTool tool, McpToolExecutor executor) {
        toolExecutors.put(tool.getName(), executor);
        log.info("Registered MCP tool: {}", tool.getName());
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
        // Create prompt with tools
        Prompt prompt = PromptBuilder.builder()
                .systemMessage(systemPrompt)
                .userMessage(userPrompt)
                .tools(convertToToolDefinitions(tools))
                .build();
        
        // Configure options for tool use
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withToolChoice("auto")
                .build();
        
        // Execute the prompt
        List<Message> response = chatClient.stream(prompt, options).collectList().block();
        
        // Process the response
        StringBuilder result = new StringBuilder();
        for (Message message : response) {
            if (message instanceof ToolMessage toolMessage) {
                // Execute the tool call
                String toolName = toolMessage.getToolCallId();
                Map<String, Object> arguments = toolMessage.getContent();
                
                McpToolExecutor executor = toolExecutors.get(toolName);
                if (executor != null) {
                    String toolResult = executor.execute(arguments);
                    result.append("[Tool: ").append(toolName).append("] ").append(toolResult).append("\n");
                } else {
                    result.append("[Unknown tool: ").append(toolName).append("]\n");
                }
            } else if (message instanceof UserMessage userMessage) {
                result.append(userMessage.getContent()).append("\n");
            } else {
                result.append(message.getContent()).append("\n");
            }
        }
        
        return result.toString();
    }

    /**
     * Converts MCP tools to tool definitions for the AI client.
     *
     * @param tools The MCP tools
     * @return The tool definitions
     */
    private List<Map<String, Object>> convertToToolDefinitions(List<McpTool> tools) {
        List<Map<String, Object>> toolDefinitions = new ArrayList<>();
        
        for (McpTool tool : tools) {
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