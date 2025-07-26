package com.cloudcompilerr.ai.service;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing MCP tools.
 * This service provides methods for retrieving and managing available tools.
 * Updated to use Java 21 features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolService {

    private final McpService mcpService;

    /**
     * Gets all available MCP tools.
     * Uses Java 21 features for more functional approach.
     *
     * @return List of available MCP tools
     */
    public List<McpTool> getAvailableTools() {
        // Get registered tool names from McpService
        List<String> toolNames = mcpService.getRegisteredToolNames();
        
        // Using Java 21 enhanced Stream API features
        return toolNames.stream()
                .map(this::createToolFromName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a tool object from a tool name.
     * Uses Java 21 pattern matching and switch expressions.
     *
     * @param name The tool name
     * @return Optional containing the tool if it can be created
     */
    private Optional<McpTool> createToolFromName(String name) {
        // Using Java 21 switch expressions with pattern matching
        return switch (name) {
            case "get_current_weather" -> Optional.of(createWeatherTool());
            case "analyze_text" -> Optional.of(createTextAnalyzerTool());
            case "analyze_file" -> Optional.of(createFileAnalyzerTool());
            default -> {
                if (name != null && !name.isEmpty()) {
                    log.warn("Unknown tool name: {}, creating generic tool", name);
                    yield Optional.of(McpTool.builder()
                            .name(name)
                            .description("Tool: " + name)
                            .build());
                } else {
                    log.warn("Null or empty tool name provided");
                    yield Optional.empty();
                }
            }
        };
    }
    
    /**
     * Creates the weather tool definition.
     * 
     * @return Weather tool definition
     */
    private McpTool createWeatherTool() {
        // Using Java 21 enhanced map creation
        Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
        
        parameters.put("location", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The city and state, e.g. San Francisco, CA")
                .required(true)
                .build());
        
        parameters.put("unit", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The unit of temperature, either 'celsius' or 'fahrenheit'")
                .required(false)
                .enumValues(List.of("celsius", "fahrenheit"))
                .build());
        
        return McpTool.builder()
                .name("get_current_weather")
                .description("Get the current weather in a given location")
                .parameters(parameters)
                .required(false)
                .build();
    }
    
    /**
     * Creates the text analyzer tool definition.
     * 
     * @return Text analyzer tool definition
     */
    private McpTool createTextAnalyzerTool() {
        Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
        
        parameters.put("text", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The text to analyze")
                .required(true)
                .build());
        
        parameters.put("analysisType", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The type of analysis to perform")
                .required(true)
                .enumValues(List.of("statistics", "sentiment", "entities", "summary"))
                .build());
        
        return McpTool.builder()
                .name("analyze_text")
                .description("Analyze text for statistics, sentiment, entities, or generate a summary")
                .parameters(parameters)
                .required(false)
                .build();
    }
    
    /**
     * Creates the file analyzer tool definition.
     * 
     * @return File analyzer tool definition
     */
    private McpTool createFileAnalyzerTool() {
        Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
        
        parameters.put("path", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The file or directory path to analyze")
                .required(true)
                .build());
        
        parameters.put("analysisType", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The type of analysis to perform")
                .required(true)
                .enumValues(List.of("fileInfo", "directoryStructure", "fileContent", "fileStats"))
                .build());
        
        parameters.put("maxDepth", McpTool.ParameterInfo.builder()
                .type("number")
                .description("Maximum depth for directory traversal (for directoryStructure analysis)")
                .required(false)
                .build());
        
        return McpTool.builder()
                .name("analyze_file")
                .description("Analyze files and directories for information, structure, content, or statistics")
                .parameters(parameters)
                .required(false)
                .build();
    }

    /**
     * Gets a tool by name.
     * Uses Java 21 enhanced Stream API and Optional features.
     *
     * @param name The tool name
     * @return The tool if found, null otherwise
     */
    public McpTool getToolByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        // Check if the tool is registered
        if (!mcpService.isToolRegistered(name)) {
            log.warn("Tool not registered: {}", name);
            return null;
        }
        
        // Using Java 21 pattern matching for instanceof
        return createToolFromName(name).orElse(null);
    }
    
    /**
     * Executes a tool with the given name and arguments.
     * Uses Java 21 enhanced error handling and pattern matching.
     *
     * @param name The tool name
     * @param arguments The tool arguments
     * @return The result of the tool execution
     */
    public String executeTool(String name, Map<String, Object> arguments) {
        record ToolExecution(String name, Map<String, Object> args) {}
        
        // Using Java 21 record patterns and pattern matching
        ToolExecution execution = new ToolExecution(name, arguments);
        
        try {
                // Simplified version without pattern matching
            String toolName = execution.name();
            Map<String, Object> args = execution.args();
            
            if (toolName == null || toolName.isEmpty()) {
                return "Error: Tool name cannot be empty";
            } else if (!mcpService.isToolRegistered(toolName)) {
                return "Error: Tool not registered: " + toolName;
            } else if (args == null) {
                return "Error: Tool arguments cannot be null";
            } else {
                return mcpService.executeWithTools(
                    "Execute the " + toolName + " tool with these arguments: " + args,
                    "You are a helpful assistant with access to tools. Use the provided tool to fulfill the request.",
                    List.of(getToolByName(toolName))
                );
            }
        } catch (Exception e) {
            log.error("Error executing tool: {}", name, e);
            return "Error executing tool: " + e.getMessage();
        }
    }
}