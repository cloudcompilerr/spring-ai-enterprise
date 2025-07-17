package com.example.ai.mcp.tools;

import com.example.ai.mcp.model.McpTool;
import com.example.ai.mcp.service.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileAnalyzerToolTest {

    @Mock
    private McpService mcpService;

    @Captor
    private ArgumentCaptor<McpTool> toolCaptor;

    @Captor
    private ArgumentCaptor<McpService.McpToolExecutor> executorCaptor;

    private FileAnalyzerTool fileAnalyzerTool;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileAnalyzerTool = new FileAnalyzerTool(mcpService);
    }

    @Test
    @DisplayName("Should register file analyzer tool on initialization")
    void shouldRegisterToolOnInit() {
        // When
        fileAnalyzerTool.register();

        // Then
        verify(mcpService).registerTool(toolCaptor.capture(), executorCaptor.capture());
        
        McpTool capturedTool = toolCaptor.getValue();
        assertEquals("analyze_file", capturedTool.getName());
        assertFalse(capturedTool.isRequired());
        assertEquals(3, capturedTool.getParameters().size());
        assertTrue(capturedTool.getParameters().containsKey("path"));
        assertTrue(capturedTool.getParameters().containsKey("analysisType"));
        assertTrue(capturedTool.getParameters().containsKey("maxDepth"));
    }

    @Test
    @DisplayName("Should analyze file info correctly")
    void shouldAnalyzeFileInfo() throws IOException {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is a test file content");
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", testFile.toString());
        args.put("analysisType", "fileInfo");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("File Information for"));
        assertTrue(result.contains("Type: Regular File"));
        assertTrue(result.contains("Size:"));
    }

    @Test
    @DisplayName("Should analyze directory structure correctly")
    void shouldAnalyzeDirectoryStructure() throws IOException {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        // Create test directory structure
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Content 1");
        Files.writeString(tempDir.resolve("file2.txt"), "Content 2");
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", tempDir.toString());
        args.put("analysisType", "directoryStructure");
        args.put("maxDepth", 2);
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Directory Structure for"));
        assertTrue(result.contains("subdir/"));
        assertTrue(result.contains("file2.txt"));
    }

    @Test
    @DisplayName("Should analyze file content correctly")
    void shouldAnalyzeFileContent() throws IOException {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is a test file content\nWith multiple lines\nFor testing purposes");
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", testFile.toString());
        args.put("analysisType", "fileContent");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("File Content Analysis for"));
        assertTrue(result.contains("Line Count: 3"));
        assertTrue(result.contains("Preview"));
    }

    @Test
    @DisplayName("Should analyze file stats correctly")
    void shouldAnalyzeFileStats() throws IOException {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is a test file content\nWith multiple lines\nFor testing purposes");
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", testFile.toString());
        args.put("analysisType", "fileStats");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("File Statistics for"));
        assertTrue(result.contains("Size:"));
        assertTrue(result.contains("Lines: 3"));
        assertTrue(result.contains("Words:"));
        assertTrue(result.contains("Characters:"));
    }

    @Test
    @DisplayName("Should analyze directory stats correctly")
    void shouldAnalyzeDirectoryStats() throws IOException {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        // Create test directory structure
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Content 1");
        Files.writeString(tempDir.resolve("file2.txt"), "Content 2");
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", tempDir.toString());
        args.put("analysisType", "fileStats");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Directory Statistics for"));
        assertTrue(result.contains("Total Files: 2"));
        assertTrue(result.contains("Total Subdirectories: 1"));
    }

    @Test
    @DisplayName("Should handle invalid arguments")
    void shouldHandleInvalidArguments() {
        // Given
        fileAnalyzerTool.register();
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
    @DisplayName("Should handle non-existent path")
    void shouldHandleNonExistentPath() {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", "/non/existent/path");
        args.put("analysisType", "fileInfo");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error: Path does not exist"));
    }

    @Test
    @DisplayName("Should handle unknown analysis type")
    void shouldHandleUnknownAnalysisType() throws IOException {
        // Given
        fileAnalyzerTool.register();
        verify(mcpService).registerTool(any(), executorCaptor.capture());
        
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Test content");
        
        McpService.McpToolExecutor executor = executorCaptor.getValue();
        Map<String, Object> args = new HashMap<>();
        args.put("path", testFile.toString());
        args.put("analysisType", "unknown");
        
        // When
        String result = executor.execute(args);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error: Unknown analysis type"));
    }
}