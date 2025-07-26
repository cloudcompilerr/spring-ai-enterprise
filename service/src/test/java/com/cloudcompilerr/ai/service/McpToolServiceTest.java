package com.cloudcompilerr.ai.service;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests for the McpToolService class.
 * Uses Java 21 features for enhanced testing.
 */
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
        // We need to use lenient here because not all tools will be checked in the test
        lenient().when(mcpService.isToolRegistered(anyString())).thenReturn(true);

        // When
        List<McpTool> tools = mcpToolService.getAvailableTools();

        // Then
        assertNotNull(tools);
        assertEquals(4, tools.size());
        
        // Using Java 21 enhanced Stream API for assertions
        assertTrue(tools.stream().anyMatch(tool -> "get_current_weather".equals(tool.getName())));
        assertTrue(tools.stream().anyMatch(tool -> "analyze_text".equals(tool.getName())));
        assertTrue(tools.stream().anyMatch(tool -> "analyze_file".equals(tool.getName())));
        assertTrue(tools.stream().anyMatch(tool -> "unknown_tool".equals(tool.getName())));
    }

    // Using Java 21 record patterns in parameterized tests
    record ToolTestCase(String toolName, boolean isRegistered, boolean shouldExist) {}
    
    @ParameterizedTest
    @MethodSource("toolTestCases")
    @DisplayName("Should get tool by name based on registration status")
    void shouldGetToolByName(ToolTestCase testCase) {
        // Extract fields from the record
        String toolName = testCase.toolName();
        boolean isRegistered = testCase.isRegistered();
        boolean shouldExist = testCase.shouldExist();
        
        // Given
        // Only set up the mock if the toolName is not null
        if (toolName != null) {
            when(mcpService.isToolRegistered(toolName)).thenReturn(isRegistered);
        }

        // When
        McpTool tool = mcpToolService.getToolByName(toolName);

        // Then
        if (shouldExist) {
            assertNotNull(tool);
            assertEquals(toolName, tool.getName());
        } else {
            assertNull(tool);
        }
    }
    
    // Test data provider using Java 21 features
    static Stream<Arguments> toolTestCases() {
        return Stream.of(
            Arguments.of(new ToolTestCase("get_current_weather", true, true)),
            Arguments.of(new ToolTestCase("analyze_text", true, true)),
            Arguments.of(new ToolTestCase("analyze_file", true, true)),
            Arguments.of(new ToolTestCase("unknown_tool", false, false)),
            Arguments.of(new ToolTestCase(null, false, false))
        );
    }

    @Test
    @DisplayName("Should execute tool")
    void shouldExecuteTool() {
        // Given
        String toolName = "get_current_weather";
        Map<String, Object> args = new HashMap<>();
        args.put("location", "San Francisco, CA");
        
        // Create a spy of the service to avoid mocking its own methods
        McpToolService serviceSpy = spy(mcpToolService);
        
        when(mcpService.isToolRegistered(toolName)).thenReturn(true);
        when(mcpService.executeWithTools(anyString(), anyString(), anyList()))
                .thenReturn("Weather in San Francisco: Sunny, 72°F");
        
        // Use doReturn for the spy to avoid the actual method being called
        McpTool weatherTool = McpTool.builder().name(toolName).description("Get weather").build();
        doReturn(weatherTool).when(serviceSpy).getToolByName(toolName);

        // When
        String result = serviceSpy.executeTool(toolName, args);

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
        
        // Create a spy of the service to avoid mocking its own methods
        McpToolService serviceSpy = spy(mcpToolService);
        
        when(mcpService.isToolRegistered(toolName)).thenReturn(true);
        when(mcpService.executeWithTools(anyString(), anyString(), anyList()))
                .thenThrow(new RuntimeException("Test exception"));
        
        // Use doReturn for the spy to avoid the actual method being called
        McpTool weatherTool = McpTool.builder().name(toolName).description("Get weather").build();
        doReturn(weatherTool).when(serviceSpy).getToolByName(toolName);

        // When
        String result = serviceSpy.executeTool(toolName, args);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error executing tool"));
    }

    // Using Java 21 record for test cases
    record InvalidToolCase(String toolName, Map<String, Object> args, String expectedErrorPrefix) {}
    
    @ParameterizedTest
    @MethodSource("invalidToolCases")
    @DisplayName("Should handle invalid tool execution requests")
    void shouldHandleInvalidToolExecutionRequests(InvalidToolCase testCase) {
        // Extract fields from the record
        String toolName = testCase.toolName();
        Map<String, Object> args = testCase.args();
        String expectedErrorPrefix = testCase.expectedErrorPrefix();
        
        // Create a custom implementation for this test to avoid mocking issues
        McpToolService customService = new McpToolService(mcpService) {
            @Override
            public McpTool getToolByName(String name) {
                if (name != null && name.equals("get_current_weather") && 
                    expectedErrorPrefix.contains("Tool arguments cannot be null")) {
                    return McpTool.builder().name(name).description("Get weather").build();
                }
                return super.getToolByName(name);
            }
        };
        
        // Given
        // Set up different behaviors based on the test case
        if (toolName == null || toolName.isEmpty()) {
            // No need to set up anything for null/empty tool name
        } else if (expectedErrorPrefix.contains("Tool not registered")) {
            lenient().when(mcpService.isToolRegistered(toolName)).thenReturn(false);
        } else if (expectedErrorPrefix.contains("Tool arguments cannot be null")) {
            lenient().when(mcpService.isToolRegistered(toolName)).thenReturn(true);
        }

        // When
        String result = customService.executeTool(toolName, args);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith(expectedErrorPrefix), 
                "Expected result to start with '" + expectedErrorPrefix + "' but was: " + result);
    }
    
    // Test data provider using Java 21 features
    static Stream<Arguments> invalidToolCases() {
        return Stream.of(
            Arguments.of(new InvalidToolCase(null, Map.of("key", "value"), "Error: Tool name cannot be empty")),
            Arguments.of(new InvalidToolCase("", Map.of("key", "value"), "Error: Tool name cannot be empty")),
            Arguments.of(new InvalidToolCase("unknown_tool", Map.of("key", "value"), "Error: Tool not registered")),
            Arguments.of(new InvalidToolCase("get_current_weather", null, "Error: Tool arguments cannot be null"))
        );
    }
    
    @Test
    @DisplayName("Should create weather tool with correct parameters")
    void shouldCreateWeatherToolWithCorrectParameters() {
        // Given
        when(mcpService.isToolRegistered("get_current_weather")).thenReturn(true);
        
        // When
        McpTool weatherTool = mcpToolService.getToolByName("get_current_weather");
        
        // Then
        assertNotNull(weatherTool);
        assertEquals("get_current_weather", weatherTool.getName());
        assertEquals("Get the current weather in a given location", weatherTool.getDescription());
        
        // Using Java 21 pattern matching for instanceof
        var parameters = weatherTool.getParameters();
        assertNotNull(parameters);
        
        // Check location parameter
        var locationParam = parameters.get("location");
        assertNotNull(locationParam);
        assertEquals("string", locationParam.getType());
        assertTrue(locationParam.isRequired());
        
        // Check unit parameter
        var unitParam = parameters.get("unit");
        assertNotNull(unitParam);
        assertEquals("string", unitParam.getType());
        assertFalse(unitParam.isRequired());
        assertNotNull(unitParam.getEnumValues());
        assertEquals(2, unitParam.getEnumValues().size());
        assertTrue(unitParam.getEnumValues().contains("celsius"));
        assertTrue(unitParam.getEnumValues().contains("fahrenheit"));
    }
    
    @Test
    @DisplayName("Should create text analyzer tool with correct parameters")
    void shouldCreateTextAnalyzerToolWithCorrectParameters() {
        // Given
        when(mcpService.isToolRegistered("analyze_text")).thenReturn(true);
        
        // When
        McpTool textTool = mcpToolService.getToolByName("analyze_text");
        
        // Then
        assertNotNull(textTool);
        assertEquals("analyze_text", textTool.getName());
        assertEquals("Analyze text for statistics, sentiment, entities, or generate a summary", textTool.getDescription());
        
        // Using Java 21 pattern matching for instanceof
        var parameters = textTool.getParameters();
        assertNotNull(parameters);
        
        // Check text parameter
        var textParam = parameters.get("text");
        assertNotNull(textParam);
        assertEquals("string", textParam.getType());
        assertTrue(textParam.isRequired());
        
        // Check analysisType parameter
        var analysisTypeParam = parameters.get("analysisType");
        assertNotNull(analysisTypeParam);
        assertEquals("string", analysisTypeParam.getType());
        assertTrue(analysisTypeParam.isRequired());
        assertNotNull(analysisTypeParam.getEnumValues());
        assertEquals(4, analysisTypeParam.getEnumValues().size());
        assertTrue(analysisTypeParam.getEnumValues().contains("statistics"));
        assertTrue(analysisTypeParam.getEnumValues().contains("sentiment"));
        assertTrue(analysisTypeParam.getEnumValues().contains("entities"));
        assertTrue(analysisTypeParam.getEnumValues().contains("summary"));
    }
    
    @Test
    @DisplayName("Should create file analyzer tool with correct parameters")
    void shouldCreateFileAnalyzerToolWithCorrectParameters() {
        // Given
        when(mcpService.isToolRegistered("analyze_file")).thenReturn(true);
        
        // When
        McpTool fileTool = mcpToolService.getToolByName("analyze_file");
        
        // Then
        assertNotNull(fileTool);
        assertEquals("analyze_file", fileTool.getName());
        assertEquals("Analyze files and directories for information, structure, content, or statistics", fileTool.getDescription());
        
        // Using Java 21 pattern matching for instanceof
        var parameters = fileTool.getParameters();
        assertNotNull(parameters);
        
        // Check path parameter
        var pathParam = parameters.get("path");
        assertNotNull(pathParam);
        assertEquals("string", pathParam.getType());
        assertTrue(pathParam.isRequired());
        
        // Check analysisType parameter
        var analysisTypeParam = parameters.get("analysisType");
        assertNotNull(analysisTypeParam);
        assertEquals("string", analysisTypeParam.getType());
        assertTrue(analysisTypeParam.isRequired());
        assertNotNull(analysisTypeParam.getEnumValues());
        assertEquals(4, analysisTypeParam.getEnumValues().size());
        assertTrue(analysisTypeParam.getEnumValues().contains("fileInfo"));
        assertTrue(analysisTypeParam.getEnumValues().contains("directoryStructure"));
        assertTrue(analysisTypeParam.getEnumValues().contains("fileContent"));
        assertTrue(analysisTypeParam.getEnumValues().contains("fileStats"));
        
        // Check maxDepth parameter
        var maxDepthParam = parameters.get("maxDepth");
        assertNotNull(maxDepthParam);
        assertEquals("number", maxDepthParam.getType());
        assertFalse(maxDepthParam.isRequired());
    }
}