package com.example.ai.rag.service;

import com.example.ai.core.config.AiProperties;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingClient embeddingClient;
    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    // System prompt template for RAG
    private static final PromptTemplate RAG_SYSTEM_PROMPT = PromptTemplate.builder()
            .template("You are a helpful assistant that answers questions based on the provided context. " +
                    "If the answer is not in the context, say so. " +
                    "Always cite the source of your information from the context.")
            .build();

    // User prompt template for RAG
    private static final PromptTemplate RAG_USER_PROMPT = PromptTemplate.builder()
            .template("Context information is below.\n" +
                    "---------------------\n" +
                    "{context}\n" +
                    "---------------------\n\n" +
                    "Given the context information and not prior knowledge, answer the question: {question}")
            .build();

    /**
     * Performs RAG to answer a question based on retrieved documents.
     *
     * @param question The user's question
     * @return The AI-generated answer
     */
    public String answerQuestion(String question) {
        // Create embedding for the question
        float[] questionEmbedding = embeddingClient.embed(question).getEmbedding();
        
        // Retrieve relevant document chunks
        List<DocumentChunk> relevantChunks = documentChunkRepository.findSimilarChunksWithThreshold(
                questionEmbedding,
                aiProperties.getRag().getSimilarityThreshold(),
                aiProperties.getRag().getTopK());
        
        if (relevantChunks.isEmpty()) {
            log.info("No relevant chunks found for question: {}", question);
            return "I don't have enough information to answer this question.";
        }
        
        // Build context from retrieved chunks
        String context = buildContext(relevantChunks);
        
        // Create prompt with context and question
        Map<String, String> promptVars = new HashMap<>();
        promptVars.put("context", context);
        promptVars.put("question", question);
        
        String userPromptText = RAG_USER_PROMPT.format(promptVars);
        String systemPromptText = RAG_SYSTEM_PROMPT.format();
        
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPromptText));
        messages.add(new UserMessage(userPromptText));
        
        Prompt prompt = new Prompt(messages);
        
        // Generate answer
        String answer = chatClient.call(prompt).getResult().getOutput().getContent();
        log.info("Generated answer for question: {}", question);
        
        return answer;
    }

    /**
     * Builds a context string from retrieved document chunks.
     *
     * @param chunks The retrieved document chunks
     * @return The context string
     */
    private String buildContext(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    String docTitle = chunk.getDocument().getTitle();
                    String docId = chunk.getDocument().getId().toString();
                    return "Document: " + docTitle + " (ID: " + docId + ")\n" + chunk.getContent();
                })
                .collect(Collectors.joining("\n\n"));
    }
}