package com.example.ai.prompt.templates;

import com.example.ai.prompt.model.PromptTemplate;
import java.util.Map;

/**
 * Collection of system prompt templates for different use cases.
 * These templates define the behavior and capabilities of the AI model.
 */
public class SystemPromptTemplates {

    /**
     * General purpose system prompt template.
     */
    public static final PromptTemplate GENERAL_PURPOSE = PromptTemplate.builder()
            .template("You are a helpful AI assistant. {additional_instructions}")
            .build();

    /**
     * System prompt template for code generation.
     */
    public static final PromptTemplate CODE_GENERATION = PromptTemplate.builder()
            .template("You are an expert software developer specializing in {language}. " +
                    "Write clean, efficient, and well-documented code. " +
                    "Follow best practices for {language} development. " +
                    "{additional_instructions}")
            .defaultValues(Map.of("additional_instructions", "Include comments to explain complex logic."))
            .build();

    /**
     * System prompt template for data analysis.
     */
    public static final PromptTemplate DATA_ANALYSIS = PromptTemplate.builder()
            .template("You are a data analyst with expertise in {domain}. " +
                    "Analyze the provided data and provide insights. " +
                    "Be thorough and consider all relevant factors. " +
                    "{additional_instructions}")
            .defaultValues(Map.of("additional_instructions", "Present your findings in a clear and concise manner."))
            .build();

    /**
     * System prompt template for summarization.
     */
    public static final PromptTemplate SUMMARIZATION = PromptTemplate.builder()
            .template("Summarize the following text in a {style} style. " +
                    "Focus on the key points and main ideas. " +
                    "{additional_instructions}")
            .defaultValues(Map.of(
                    "style", "concise",
                    "additional_instructions", "Maintain the original meaning while being brief."))
            .build();

    /**
     * System prompt template for question answering.
     */
    public static final PromptTemplate QUESTION_ANSWERING = PromptTemplate.builder()
            .template("Answer the following question based on the provided context. " +
                    "Be accurate and provide evidence from the context. " +
                    "If the answer is not in the context, say so. " +
                    "{additional_instructions}")
            .defaultValues(Map.of("additional_instructions", "Be concise and to the point."))
            .build();
}