package com.example.ai.service;

import com.example.ai.mcp.model.McpTool;
import com.example.ai.mcp.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing MCP tools.
 * This service provides methods for retrieving available tools.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolService {

    private final McpService mcpService;

    /**
     * Gets all available MCP tools.
     *
     * @return List of available MCP tools
     */
    public List<McpTool> getAvailableTools() {
        // In a real implementation, this would retrieve tools from the MCP service
        // For now, we'll return a simple list of tools
        List<McpTool> tools = new ArrayList<>();
        
        // Add weather tool
        tools.add(McpTool.builder()
                .name("get_current_weather")
                .description("Get the current weather in a given location")
                .build());
        
        return tools;
    }

    /**
     * Gets a tool by name.
     *
     * @param name The tool name
     * @return The tool if found, null otherwise
     */
    public McpTool getToolByName(String name) {
        return getAvailableTools().stream()
                .filter(tool -> tool.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}