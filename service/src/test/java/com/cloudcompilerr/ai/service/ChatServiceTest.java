package com.cloudcompilerr.ai.service;

import com.cloudcompilerr.ai.core.config.AiProperties;
import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import com.cloudcompilerr.ai.prompt.service.PromptEngineeringService;
import com.cloudcompilerr.ai.prompt.templates.SystemPromptTemplates;
import com.cloudcompilerr.ai.prompt.model.PromptTemplate;
import com.cloudcompilerr.ai.rag.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient chatClient;
    
    @Mock
    private RagService ragService;
    
    @Mock
    private PromptEngineeringService promptEngineeringService;
    
    @Mock
    private McpService mcpService;
    
    @Mock
    private McpToolService mcpToolService;
    
    @Mock
    private AiProperties aiProperties;
    
    @Mock
    private ChatResponse chatResponse;
    
    @Mock
    private Generation generation;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                chatClient, 
                ragService, 
                promptEngineeringService, 
                mcpService, 
                mcpToolService, 
                aiProperties
        );
    }

    @Test
    @DisplayName("Should generate chat response with system prompt")
    void shouldGenerateChatResponseWithSystemPrompt() {
        // Given
        String userPrompt = "Tell me about Java 21";
        String systemPrompt = "You are a Java expert";
        String expectedResponse = "Java 21 is the latest LTS release with many new features";
        
        // Create an AssistantMessage with the expected response
        org.springframework.ai.chat.messages.AssistantMessage assistantMessage = 
            new org.springframework.ai.chat.messages.AssistantMessage(expectedResponse);
        
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        
        // When
        String response = chatService.generateChatResponse(userPrompt, systemPrompt);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(chatClient).call(any(Prompt.class));
    }

    @Test
    @DisplayName("Should generate chat response with default system prompt")
    void shouldGenerateChatResponseWithDefaultSystemPrompt() {
        // Given
        String userPrompt = "Tell me about Java 21";
        String expectedResponse = "Java 21 is the latest LTS release with many new features";
        
        // Create an AssistantMessage with the expected response
        org.springframework.ai.chat.messages.AssistantMessage assistantMessage = 
            new org.springframework.ai.chat.messages.AssistantMessage(expectedResponse);
        
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        
        // When
        String response = chatService.generateChatResponse(userPrompt, null);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(chatClient).call(any(Prompt.class));
    }

    @Test
    @DisplayName("Should generate RAG response")
    void shouldGenerateRagResponse() {
        // Given
        String userPrompt = "What is Spring AI?";
        String expectedResponse = "Spring AI is a framework for building AI applications";
        
        when(ragService.answerQuestion(userPrompt)).thenReturn(expectedResponse);
        
        // When
        String response = chatService.generateRagResponse(userPrompt);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(ragService).answerQuestion(userPrompt);
    }

    // Using Java 21 style parameterized tests with record patterns
    @ParameterizedTest
    @MethodSource("templateTestCases")
    @DisplayName("Should generate templated response based on template name")
    void shouldGenerateTemplatedResponse(TemplateTestCase testCase) {
        // Extract fields from the record
        String templateName = testCase.templateName();
        Map<String, String> variables = testCase.variables();
        PromptTemplate expectedTemplate = testCase.expectedTemplate();
        
        // Given
        String expectedResponse = "Generated response";
        
        when(promptEngineeringService.executePrompt(
                eq(expectedTemplate),
                anyMap(),
                any(),
                anyMap()
        )).thenReturn(expectedResponse);
        
        // When
        String response = chatService.generateTemplatedResponse(templateName, variables);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(promptEngineeringService).executePrompt(
                eq(expectedTemplate),
                argThat(map -> !map.containsKey("prompt")),
                any(),
                any()
        );
    }
    
    // Using Java 21 record for test case data
    record TemplateTestCase(String templateName, Map<String, String> variables, PromptTemplate expectedTemplate) {}
    
    // Test data provider using Java 21 features
    static Stream<Arguments> templateTestCases() {
        return Stream.of(
            Arguments.of(new TemplateTestCase(
                "code",
                Map.of("prompt", "Write a Java function", "language", "Java"),
                SystemPromptTemplates.CODE_GENERATION
            )),
            Arguments.of(new TemplateTestCase(
                "data",
                Map.of("prompt", "Analyze this dataset", "format", "CSV"),
                SystemPromptTemplates.DATA_ANALYSIS
            )),
            Arguments.of(new TemplateTestCase(
                "summary",
                Map.of("prompt", "Summarize this text"),
                SystemPromptTemplates.SUMMARIZATION
            )),
            Arguments.of(new TemplateTestCase(
                "qa",
                Map.of("prompt", "What is Java?"),
                SystemPromptTemplates.QUESTION_ANSWERING
            )),
            Arguments.of(new TemplateTestCase(
                "unknown",
                Map.of("prompt", "Some prompt"),
                SystemPromptTemplates.GENERAL_PURPOSE
            ))
        );
    }

    @Test
    @DisplayName("Should generate tool-augmented response")
    void shouldGenerateToolAugmentedResponse() {
        // Given
        String userPrompt = "What's the weather in New York?";
        String systemPrompt = "You are a helpful assistant";
        List<String> toolNames = List.of("get_current_weather", "analyze_text");
        String expectedResponse = "The weather in New York is sunny";
        
        // Using Java 21 enhanced Stream API with pattern matching
        var weatherTool = McpTool.builder()
                .name("get_current_weather")
                .description("Get weather information")
                .build();
                
        var textTool = McpTool.builder()
                .name("analyze_text")
                .description("Analyze text")
                .build();
        
        when(mcpToolService.getToolByName("get_current_weather")).thenReturn(weatherTool);
        when(mcpToolService.getToolByName("analyze_text")).thenReturn(textTool);
        when(mcpService.executeWithTools(
                eq(userPrompt),
                eq(systemPrompt),
                argThat(tools -> tools.size() == 2)
        )).thenReturn(expectedResponse);
        
        // When
        String response = chatService.generateToolAugmentedResponse(userPrompt, systemPrompt, toolNames);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(mcpService).executeWithTools(
                eq(userPrompt),
                eq(systemPrompt),
                argThat(tools -> 
                    tools.size() == 2 && 
                    tools.get(0).getName().equals("get_current_weather") &&
                    tools.get(1).getName().equals("analyze_text")
                )
        );
    }

    @Test
    @DisplayName("Should generate tool-augmented response with default system prompt")
    void shouldGenerateToolAugmentedResponseWithDefaultSystemPrompt() {
        // Given
        String userPrompt = "What's the weather in New York?";
        List<String> toolNames = List.of("get_current_weather");
        String expectedResponse = "The weather in New York is sunny";
        String defaultSystemPrompt = "You are a helpful assistant with access to tools. Use the tools when appropriate to answer the user's question.";
        
        var weatherTool = McpTool.builder()
                .name("get_current_weather")
                .description("Get weather information")
                .build();
                
        when(mcpToolService.getToolByName("get_current_weather")).thenReturn(weatherTool);
        when(mcpService.executeWithTools(
                eq(userPrompt),
                eq(defaultSystemPrompt),
                argThat(tools -> tools.size() == 1)
        )).thenReturn(expectedResponse);
        
        // When
        String response = chatService.generateToolAugmentedResponse(userPrompt, null, toolNames);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(mcpService).executeWithTools(
                eq(userPrompt),
                eq(defaultSystemPrompt),
                argThat(tools -> 
                    tools.size() == 1 && 
                    tools.get(0).getName().equals("get_current_weather")
                )
        );
    }
    
    @Test
    @DisplayName("Should handle null tool names in tool-augmented response")
    void shouldHandleNullToolNamesInToolAugmentedResponse() {
        // Given
        String userPrompt = "What's the weather in New York?";
        String systemPrompt = "You are a helpful assistant";
        List<String> toolNames = List.of("get_current_weather", "non_existent_tool");
        String expectedResponse = "The weather in New York is sunny";
        
        var weatherTool = McpTool.builder()
                .name("get_current_weather")
                .description("Get weather information")
                .build();
                
        when(mcpToolService.getToolByName("get_current_weather")).thenReturn(weatherTool);
        when(mcpToolService.getToolByName("non_existent_tool")).thenReturn(null);
        when(mcpService.executeWithTools(
                eq(userPrompt),
                eq(systemPrompt),
                argThat(tools -> tools.size() == 1)
        )).thenReturn(expectedResponse);
        
        // When
        String response = chatService.generateToolAugmentedResponse(userPrompt, systemPrompt, toolNames);
        
        // Then
        assertEquals(expectedResponse, response);
        verify(mcpService).executeWithTools(
                eq(userPrompt),
                eq(systemPrompt),
                argThat(tools -> 
                    tools.size() == 1 && 
                    tools.get(0).getName().equals("get_current_weather")
                )
        );
    }
}