package com.cloudcompilerr.ai.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration for Spring Retry functionality.
 */
@Configuration
@EnableRetry
public class RetryConfig {
    // Spring Retry is enabled via @EnableRetry annotation
    // Individual retry configurations are handled via @Retryable annotations
}