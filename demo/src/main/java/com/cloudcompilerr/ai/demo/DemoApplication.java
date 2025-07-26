package com.cloudcompilerr.ai.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Spring AI Showcase demo.
 * This class bootstraps the entire application.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.cloudcompilerr.ai")
@EntityScan(basePackages = "com.cloudcompilerr.ai")
@EnableJpaRepositories(basePackages = "com.cloudcompilerr.ai")
public class DemoApplication {

    /**
     * Main method to start the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}