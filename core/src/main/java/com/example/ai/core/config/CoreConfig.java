package com.example.ai.core.config;

import org.springframework.context.annotation.Configuration;

/**
 * Core configuration for Spring AI components.
 * This class provides the basic configuration needed across the application.
 * 
 * Spring AI 0.8.1 auto-configuration will automatically create the necessary
 * EmbeddingClient and ChatClient beans when the appropriate starters are included.
 */
@Configuration
public class CoreConfig {
    // Spring AI auto-configuration will handle bean creation
    // when spring-ai-openai-spring-boot-starter is on the classpath
}