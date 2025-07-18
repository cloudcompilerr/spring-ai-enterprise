package com.example.ai.rag.service;

import com.example.ai.core.config.AiProperties;
import com.example.ai.core.config.AiProperties.RagConfig;
import com.example.ai.prompt.model.PromptTemplate;
import com.example.ai.rag.model.DocumentChunk;
import com.example.ai.rag.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Retrieval-Augmented Generation (RAG).
 * This service combines document retrieval with AI generation.
 * Updated to use Java 21 features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingClient embeddingClient;
    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    // System prompt template for RAG using Java 21 text blocks
    private static final PromptTemplate RAG_SYSTEM_PROMPT = PromptTemplate.builder()
            .template("""
                    You are a helpful assistant that answers questions based on the provided context.
                    If the answer is not in the context, say so.
                    Always cite the source of your information from the context.
                    """)
            .build();

    // User prompt template for RAG using Java 21 text blocks
    private static final PromptTemplate RAG_USER_PROMPT = PromptTemplate.builder()
            .template("""
                    Context information is below.
                    ---------------------
                    {context}
                    ---------------------
                    
                    Given the context information and not prior knowledge, answer the question: {question}
                    """)
            .build();

    /**
     * Performs RAG to answer a question based on retrieved documents.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param question The user's question
     * @return The AI-generated answer
     */
    public String answerQuestion(String question) {
        // Using Java 21 pattern matching for instanceof with conditional binding
        if (!(question instanceof String s) || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        
        try {
            log.debug("Creating embedding for question: {}", question);
            List<Double> embeddingList = embeddingClient.embed(question);
            float[] questionEmbedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                questionEmbedding[i] = embeddingList.get(i).floatValue();
            }
            
            // Using Java 21 records for immutable configuration
            RagConfig ragConfig = aiProperties.getRag().asRagConfig();
            
            log.debug("Retrieving relevant document chunks");
            List<DocumentChunk> relevantChunks = documentChunkRepository.findSimilarChunksWithThreshold(
                    questionEmbedding,
                    ragConfig.similarityThreshold(),
                    ragConfig.topK());
            
            // Using Java 21 enhanced switch expressions
            return switch (relevantChunks.size()) {
                case 0 -> {
                    log.info("No relevant chunks found for question: {}", question);
                    yield "I don't have enough information in my knowledge base to answer this question.";
                }
                default -> {
                    log.debug("Found {} relevant chunks", relevantChunks.size());
                    yield generateAnswerFromChunks(relevantChunks, question);
                }
            };
        } catch (Exception e) {
            log.error("Error in RAG process", e);
            throw new RuntimeException("Failed to generate answer using RAG", e);
        }
    }

    /**
     * Generates an answer from retrieved document chunks.
     * Uses Java 21 enhanced Map API and functional programming.
     *
     * @param chunks The retrieved document chunks
     * @param question The user's question
     * @return The AI-generated answer
     */
    private String generateAnswerFromChunks(List<DocumentChunk> chunks, String question) {
        String context = buildContext(chunks);
        
        // Create prompt with context and question using Java 21 Map.of
        var promptVars = Map.of(
            "context", context,
            "question", question
        );
        
        String userPromptText = RAG_USER_PROMPT.format(promptVars);
        String systemPromptText = RAG_SYSTEM_PROMPT.format();
        
        // Using Java 21 List.of for immutable list creation
        List<Message> messages = List.of(
            new SystemMessage(systemPromptText),
            new UserMessage(userPromptText)
        );
        
        Prompt prompt = new Prompt(messages);
        
        // Generate answer
        log.debug("Generating answer using AI model");
        String answer = chatClient.call(prompt).getResult().getOutput().getContent();
        log.info("Generated answer for question: {}", question);
        
        return answer;
    }

    /**
     * Builds a context string from retrieved document chunks.
     * Uses Java 21 enhanced Stream API and text blocks.
     *
     * @param chunks The retrieved document chunks
     * @return The context string
     */
    private String buildContext(List<DocumentChunk> chunks) {
        // Using Java 21 record pattern in lambda expressions
        record DocumentInfo(String title, String id, String content) {}
        
        return chunks.stream()
                .map(chunk -> new DocumentInfo(
                    chunk.getDocument().getTitle(),
                    chunk.getDocument().getId().toString(),
                    chunk.getContent()
                ))
                .map(info -> String.format(
                    "Document: %s (ID: %s)%n%s",
                    info.title(),
                    info.id(),
                    info.content()
                ))
                .collect(Collectors.joining("\n\n"));
    }
    
    /**
     * Performs RAG to answer a question with specific parameters.
     * Uses Java 21 records for parameter grouping and enhanced error handling.
     *
     * @param question The user's question
     * @param topK Number of chunks to retrieve
     * @param similarityThreshold Similarity threshold for retrieval
     * @return The AI-generated answer
     */
    public String answerQuestionWithParams(String question, int topK, float similarityThreshold) {
        // Using Java 21 pattern matching for instanceof with conditional binding
        if (!(question instanceof String s) || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        
        // Using Java 21 record for parameter grouping
        record RagParams(String question, int topK, float threshold) {}
        var params = new RagParams(question, topK, similarityThreshold);
        
        try {
            log.debug("Creating embedding for question with custom params: {}", params.question());
            List<Double> embeddingList = embeddingClient.embed(params.question());
            float[] questionEmbedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                questionEmbedding[i] = embeddingList.get(i).floatValue();
            }
            
            log.debug("Retrieving relevant document chunks with topK={}, threshold={}", 
                    params.topK(), params.threshold());
            
            List<DocumentChunk> relevantChunks = documentChunkRepository.findSimilarChunksWithThreshold(
                    questionEmbedding,
                    params.threshold(),
                    params.topK());
            
            // Using Java 21 enhanced switch expressions with pattern matching
            return switch (relevantChunks) {
                case List<DocumentChunk> chunks when chunks.isEmpty() -> {
                    log.info("No relevant chunks found for question: {}", params.question());
                    yield "I don't have enough information in my knowledge base to answer this question.";
                }
                case List<DocumentChunk> chunks -> {
                    log.debug("Found {} relevant chunks", chunks.size());
                    yield generateAnswerFromChunks(chunks, params.question());
                }
            };
        } catch (Exception e) {
            log.error("Error in RAG process with custom params", e);
            throw new RuntimeException("Failed to generate answer using RAG with custom params", e);
        }
    }
}