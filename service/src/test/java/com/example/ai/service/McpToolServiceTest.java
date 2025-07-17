package com.example.ai.service;

import com.example.ai.mcp.model.McpTool;
import com.example.ai.mcp.service.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpToolServiceTest {

    @Mock
    private McpService mcpService;

    private McpToolService mcpToolService;

    @BeforeEach
    void setUp() {
        mcpToolService = new McpToolService(mcpService);
    }

    @Test
    @DisplayName("Should get available tools")
    void shouldGetAvailableTools() {
        // Given
        when(mcpService.getRegisteredToolNames()).thenReturn(List.of(
                "get_current_weather", 
                "analyze_text", 
                "analyze_file",
                "unknown_tool"
        ));
        when(mcpService.isToolRegistered(anyString())).thenReturn(true);

        // When
        List<McpTool> tools = mcpToolService.getAvailableTools();

        // Then
        assertNotNull(tools);
        assertEquals(4, tools.size());
        
        // Verify tool names
        assertTrue(tools.stream().anyMatch(tool -> "get_current_weather".equals(tool.getName())));
        assertTrue(tools.stream().anyMatch(tool -> "analyze_text".equals(tool.getName())));
        assertTrue(tools.stream().anyMatch(tool -> "analyze_file".equals(tool.getName())));
        assertTrue(tools.stream().anyMatch(tool -> "unknown_tool".equals(tool.getName())));
    }

    @Test
    @DisplayName("Should get tool by name")
    void shouldGetToolByName() {
        // Given
        when(mcpService.isToolRegistered("get_current_weather")).thenReturn(true);
        when(mcpService.isToolRegistered("analyze_text")).thenReturn(true);
        when(mcpService.isToolRegistered("analyze_file")).thenReturn(true);
        when(mcpService.isToolRegistered("unknown_tool")).thenReturn(false);

        // When & Then
        McpTool weatherTool = mcpToolService.getToolByName("get_current_weather");
        assertNotNull(weatherTool);
        assertEquals("get_current_weather", weatherTool.getName());
        
        McpTool textTool = mcpToolService.getToolByName("analyze_text");
        assertNotNull(textTool);
        assertEquals("analyze_text", textTool.getName());
        
        McpTool fileTool = mcpToolService.getToolByName("analyze_file");
        assertNotNull(fileTool);
        assertEquals("analyze_file", fileTool.getName());
        
        McpTool unknownTool = mcpToolService.getToolByName("unknown_tool");
        assertNull(unknownTool);
        
        McpTool nullTool = mcpToolService.getToolByName(null);
        assertNull(nullTool);
    }

    @Test
    @DisplayName("Should execute tool")
    void shouldExecuteTool() {
        // Given
        String toolName = "get_current_weather";
        Map<String, Object> args = new HashMap<>();
        args.put("location", "San Francisco, CA");
        
        when(mcpService.isToolRegistered(toolName)).thenReturn(true);
        when(mcpService.executeWithTools(anyString(), anyString(), anyList()))
                .thenReturn("Weather in San Francisco: Sunny, 72°F");

        // When
        String result = mcpToolService.executeTool(toolName, args);

        // Then
        assertNotNull(result);
        assertEquals("Weather in San Francisco: Sunny, 72°F", result);
    }

    @Test
    @DisplayName("Should handle execution errors")
    void shouldHandleExecutionErrors() {
        // Given
        String toolName = "get_current_weather";
        Map<String, Object> args = new HashMap<>();
        args.put("location", "San Francisco, CA");
        
        when(mcpService.isToolRegistered(toolName)).thenReturn(true);
        when(mcpService.executeWithTools(anyString(), anyString(), anyList()))
                .thenThrow(new RuntimeException("Test exception"));

        // When
        String result = mcpToolService.executeTool(toolName, args);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error executing tool"));
    }

    @Test
    @DisplayName("Should handle invalid tool execution requests")
    void shouldHandleInvalidToolExecutionRequests() {
        // Given
        when(mcpService.isToolRegistered("unknown_tool")).thenReturn(false);

        // When & Then
        String result1 = mcpToolService.executeTool(null, Map.of("key", "value"));
        assertTrue(result1.contains("Error: Tool name cannot be empty"));
        
        String result2 = mcpToolService.executeTool("unknown_tool", Map.of("key", "value"));
        assertTrue(result2.contains("Error: Tool not registered"));
        
        String result3 = mcpToolService.executeTool("get_current_weather", null);
        assertTrue(result3.contains("Error: Tool arguments cannot be null"));
    }
}