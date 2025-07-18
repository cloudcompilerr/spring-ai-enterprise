package com.example.ai.api.controller;

import com.example.ai.api.dto.ChatRequest;
import com.example.ai.api.dto.ChatResponse;
import com.example.ai.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests for the ChatController class.
 * Uses Java 21 features for enhanced testing.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        // No setup needed as we're using constructor injection with Mockito
    }

    @Test
    @DisplayName("Should generate chat completion successfully")
    void shouldGenerateChatCompletionSuccessfully() {
        // Given
        var request = new ChatRequest("What is Java 21?", "You are a helpful assistant");
        var expectedResponse = "Java 21 is the latest LTS release with many new features";
        
        when(chatService.generateChatResponse(request.prompt(), request.systemPrompt()))
                .thenReturn(expectedResponse);
        
        // When
        ResponseEntity<ChatResponse> responseEntity = chatController.chatCompletion(request);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponse, responseEntity.getBody().response());
    }

    @Test
    @DisplayName("Should handle exception during chat completion")
    void shouldHandleExceptionDuringChatCompletion() {
        // Given
        var request = new ChatRequest("What is Java 21?", "You are a helpful assistant");
        
        when(chatService.generateChatResponse(request.prompt(), request.systemPrompt()))
                .thenThrow(new RuntimeException("Test exception"));
        
        // When & Then
        var exception = assertThrows(ResponseStatusException.class, () -> 
                chatController.chatCompletion(request));
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to generate response"));
    }

    @Test
    @DisplayName("Should generate RAG chat completion successfully")
    void shouldGenerateRagChatCompletionSuccessfully() {
        // Given
        var request = new ChatRequest("What is Spring AI?", null);
        var expectedResponse = "Spring AI is a framework for building AI applications";
        
        when(chatService.generateRagResponse(request.prompt()))
                .thenReturn(expectedResponse);
        
        // When
        ResponseEntity<ChatResponse> responseEntity = chatController.ragChatCompletion(request);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponse, responseEntity.getBody().response());
    }

    // Using Java 21 record patterns in parameterized tests
    record TemplateTestCase(String templateName, Map<String, String> variables, String expectedResponse) {}
    
    @ParameterizedTest
    @MethodSource("templateTestCases")
    @DisplayName("Should generate templated chat completion successfully")
    void shouldGenerateTemplatedChatCompletionSuccessfully(TemplateTestCase testCase) {
        // Using Java 21 record pattern in method parameter
        var (templateName, variables, expectedResponse) = testCase;
        
        // Given
        when(chatService.generateTemplatedResponse(eq(templateName), eq(variables)))
                .thenReturn(expectedResponse);
        
        // When
        ResponseEntity<ChatResponse> responseEntity = chatController.templateChatCompletion(templateName, variables);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponse, responseEntity.getBody().response());
    }
    
    // Test data provider using Java 21 features
    static Stream<Arguments> templateTestCases() {
        return Stream.of(
            Arguments.of(new TemplateTestCase(
                "code",
                Map.of("prompt", "Write a Java function", "language", "Java"),
                "Here's a Java function: public void hello() {}"
            )),
            Arguments.of(new TemplateTestCase(
                "data",
                Map.of("prompt", "Analyze this dataset", "format", "CSV"),
                "Analysis: The dataset shows a positive trend"
            ))
        );
    }

    @Test
    @DisplayName("Should throw exception when prompt is missing in template variables")
    void shouldThrowExceptionWhenPromptIsMissingInTemplateVariables() {
        // Given
        String templateName = "code";
        Map<String, String> variables = Map.of("language", "Java");
        
        // When & Then
        var exception = assertThrows(ResponseStatusException.class, () -> 
                chatController.templateChatCompletion(templateName, variables));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Prompt is required", exception.getReason());
    }

    @Test
    @DisplayName("Should generate tool-augmented chat completion successfully")
    void shouldGenerateToolAugmentedChatCompletionSuccessfully() {
        // Given
        var request = new ChatRequest("What's the weather in New York?", "You are a helpful assistant");
        List<String> toolNames = List.of("get_current_weather");
        var expectedResponse = "The weather in New York is sunny";
        
        when(chatService.generateToolAugmentedResponse(
                eq(request.prompt()), 
                eq(request.systemPrompt()),
                eq(toolNames)))
                .thenReturn(expectedResponse);
        
        // When
        ResponseEntity<ChatResponse> responseEntity = chatController.toolChatCompletion(request, toolNames);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponse, responseEntity.getBody().response());
    }

    @Test
    @DisplayName("Should handle null tool names in tool-augmented chat completion")
    void shouldHandleNullToolNamesInToolAugmentedChatCompletion() {
        // Given
        var request = new ChatRequest("What's the weather in New York?", "You are a helpful assistant");
        var expectedResponse = "I don't have access to weather information";
        
        when(chatService.generateToolAugmentedResponse(
                eq(request.prompt()), 
                eq(request.systemPrompt()),
                eq(List.of())))
                .thenReturn(expectedResponse);
        
        // When
        ResponseEntity<ChatResponse> responseEntity = chatController.toolChatCompletion(request, null);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponse, responseEntity.getBody().response());
    }

    @Test
    @DisplayName("Should process batch chat completion successfully")
    void shouldProcessBatchChatCompletionSuccessfully() {
        // Given
        var requests = List.of(
            new ChatRequest("What is Java?", null),
            new ChatRequest("What is Spring?", null)
        );
        
        when(chatService.generateChatResponse(eq("What is Java?"), isNull()))
                .thenReturn("Java is a programming language");
        
        when(chatService.generateChatResponse(eq("What is Spring?"), isNull()))
                .thenReturn("Spring is a Java framework");
        
        // When
        ResponseEntity<List<ChatResponse>> responseEntity = chatController.batchChatCompletion(requests);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals("Java is a programming language", responseEntity.getBody().get(0).response());
        assertEquals("Spring is a Java framework", responseEntity.getBody().get(1).response());
    }

    @Test
    @DisplayName("Should handle exceptions in batch chat completion")
    void shouldHandleExceptionsInBatchChatCompletion() {
        // Given
        var requests = List.of(
            new ChatRequest("What is Java?", null),
            new ChatRequest("What is Spring?", null)
        );
        
        when(chatService.generateChatResponse(eq("What is Java?"), isNull()))
                .thenReturn("Java is a programming language");
        
        when(chatService.generateChatResponse(eq("What is Spring?"), isNull()))
                .thenThrow(new RuntimeException("Test exception"));
        
        // When
        ResponseEntity<List<ChatResponse>> responseEntity = chatController.batchChatCompletion(requests);
        
        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals("Java is a programming language", responseEntity.getBody().get(0).response());
        assertTrue(responseEntity.getBody().get(1).response().contains("Error:"));
    }
}