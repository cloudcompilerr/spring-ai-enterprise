package com.cloudcompilerr.ai.mcp.tools;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextAnalyzerToolTest {

    @Mock
    private McpService mcpService;

    @Captor
    private ArgumentCaptor<McpTool> toolCaptor;

    @Captor
    private ArgumentCaptor<McpService.McpToolExecutor> executorCaptor;

    private TextAnalyzerTool textAnalyzerTool;

    @BeforeEach
    void setUp() {
        textAnalyzerTool = new TextAnalyzerTool(mcpService);
    }

    @Test
    @DisplayName("Should register text analyzer tool on initialization")
    void shouldRegisterToolOnInit() {
        // When
        textAnalyzerTool.register();

        // Then
        verify(mcpService).registerTool(toolCaptor.capture(), executorCaptor.capture());
        
        McpTool capturedTool = toolCaptor.getValue();
        assertEquals("analyze_text", capturedTool.getName());
        assertFalse(capturedTool.isRequired());
        assertEquals(2, capturedTool.getParameters().size());
        assertTrue(capturedTool.getParameters().containsKey("text"));
        assertTrue(capturedTool.getParameters().containsKey("analysisType"));
    }

    @Test
    @DisplayName("Should analyze text statistics correctly")
    void shouldAnalyzeTextStatistics() {
        // Given
        textAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("text", "This is a test sentence. This is another test sentence.");
        args.put("analysisType", "statistics");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Text Statistics:"));
        assertTrue(result.contains("Character count:"));
        assertTrue(result.contains("Word count:"));
        assertTrue(result.contains("Sentence count: 2"));
    }

    @Test
    @DisplayName("Should analyze sentiment correctly")
    void shouldAnalyzeSentiment() {
        // Given
        textAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("text", "I love this product. It's amazing and wonderful.");
        args.put("analysisType", "sentiment");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Sentiment Analysis:"));
        assertTrue(result.contains("Overall sentiment:"));
        assertTrue(result.contains("Positive") || result.contains("Very Positive"));
    }

    @Test
    @DisplayName("Should extract entities correctly")
    void shouldExtractEntities() {
        // Given
        textAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("text", "John Smith visited London last week. He works at Acme Corp.");
        args.put("analysisType", "entities");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Entity Extraction:"));
        assertTrue(result.contains("John Smith"));
        assertTrue(result.contains("London"));
    }

    @Test
    @DisplayName("Should generate summary correctly")
    void shouldGenerateSummary() {
        // Given
        textAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        String longText = "This is the first sentence of a long text. " +
                "This is the second sentence with some details. " +
                "This third sentence adds more information. " +
                "The fourth sentence is not very important. " +
                "The fifth sentence contains crucial information. " +
                "This is the final concluding sentence.";
        args.put("text", longText);
        args.put("analysisType", "summary");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Summary:"));
        assertTrue(result.length() < longText.length());
    }

    @Test
    @DisplayName("Should handle invalid arguments")
    void shouldHandleInvalidArguments() {
        // Given
        textAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        // Missing required arguments
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error:"));
    }

    @Test
    @DisplayName("Should handle unknown analysis type")
    void shouldHandleUnknownAnalysisType() {
        // Given
        textAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("text", "Some text");
        args.put("analysisType", "unknown");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error: Unknown analysis type"));
    }
}