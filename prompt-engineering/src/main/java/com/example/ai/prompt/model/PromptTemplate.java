package com.example.ai.prompt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * A template for creating prompts with variable placeholders.
 * This class allows for the creation of reusable prompt templates
 * with placeholders that can be filled in at runtime.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {

    /**
     * The template string with placeholders in the format {placeholder}.
     */
    private String template;
    
    /**
     * Default values for placeholders.
     */
    @Builder.Default
    private Map<String, String> defaultValues = new HashMap<>();
    
    /**
     * Creates a formatted prompt by replacing placeholders with values.
     * 
     * @param values The values to replace placeholders with
     * @return The formatted prompt string
     */
    public String format(Map<String, String> values) {
        String result = template;
        
        // First apply default values
        for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // Then apply provided values (overriding defaults)
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Creates a formatted prompt with a single placeholder value.
     * 
     * @param key The placeholder key
     * @param value The value to replace the placeholder with
     * @return The formatted prompt string
     */
    public String format(String key, String value) {
        Map<String, String> values = new HashMap<>();
        values.put(key, value);
        return format(values);
    }
    
    /**
     * Creates a formatted prompt with no placeholder values.
     * Uses only the default values if any are defined.
     * 
     * @return The formatted prompt string
     */
    public String format() {
        return format(new HashMap<>());
    }
}