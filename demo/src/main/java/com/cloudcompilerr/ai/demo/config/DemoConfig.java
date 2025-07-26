package com.cloudcompilerr.ai.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for the demo application.
 */
@Configuration
public class DemoConfig {

    /**
     * Creates a RestTemplate bean for making HTTP requests.
     *
     * @return The RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}