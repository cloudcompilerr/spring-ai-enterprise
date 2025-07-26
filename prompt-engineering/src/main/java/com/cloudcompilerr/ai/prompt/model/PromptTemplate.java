package com.cloudcompilerr.ai.prompt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A template for creating prompts with variable placeholders.
 * This class allows for the creation of reusable prompt templates
 * with placeholders that can be filled in at runtime.
 * Updated to use Java 21 features.
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
    
    // Using Java 21 pattern compilation for better performance
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    /**
     * Creates a formatted prompt by replacing placeholders with values.
     * Uses Java 21 enhanced String and Stream API features.
     * 
     * @param values The values to replace placeholders with
     * @return The formatted prompt string
     */
    public String format(Map<String, String> values) {
        // Using Java 21 enhanced map merging
        var mergedValues = new HashMap<>(defaultValues);
        values.forEach((key, value) -> mergedValues.put(key, value));
        
        // Using Java 21 pattern matching and functional approach
        String result = template;
        
        // Find all placeholders in the template
        var matcher = PLACEHOLDER_PATTERN.matcher(template);
        var placeholders = new HashMap<String, String>();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = mergedValues.getOrDefault(placeholder, "{" + placeholder + "}");
            placeholders.put("{" + placeholder + "}", replacement);
        }
        
        // Replace all placeholders at once
        for (var entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Creates a formatted prompt with a single placeholder value.
     * Uses Java 21 Map.of for concise map creation.
     * 
     * @param key The placeholder key
     * @param value The value to replace the placeholder with
     * @return The formatted prompt string
     */
    public String format(String key, String value) {
        return format(Map.of(key, value));
    }
    
    /**
     * Creates a formatted prompt with no placeholder values.
     * Uses only the default values if any are defined.
     * 
     * @return The formatted prompt string
     */
    public String format() {
        return format(Map.of());
    }
    
    /**
     * Creates a new PromptTemplate by combining this template with another.
     * Uses Java 21 text blocks for better readability.
     * 
     * @param other The other template to combine with
     * @return A new combined template
     */
    public PromptTemplate combine(PromptTemplate other) {
        // Using Java 21 text blocks for multi-line strings
        String combinedTemplate = """
                %s
                
                %s
                """.formatted(this.template, other.template);
        
        // Merge default values
        Map<String, String> combinedDefaults = new HashMap<>(this.defaultValues);
        combinedDefaults.putAll(other.defaultValues);
        
        return PromptTemplate.builder()
                .template(combinedTemplate)
                .defaultValues(combinedDefaults)
                .build();
    }
    
    /**
     * Creates a new PromptTemplate with additional default values.
     * Uses Java 21 functional programming enhancements.
     * 
     * @param additionalDefaults Additional default values to add
     * @return A new template with combined default values
     */
    public PromptTemplate withDefaults(Map<String, String> additionalDefaults) {
        Map<String, String> newDefaults = new HashMap<>(this.defaultValues);
        newDefaults.putAll(additionalDefaults);
        
        return PromptTemplate.builder()
                .template(this.template)
                .defaultValues(newDefaults)
                .build();
    }
    
    /**
     * Extracts placeholder keys from the template.
     * Uses Java 21 pattern matching and Stream API enhancements.
     * 
     * @return Set of placeholder keys
     */
    public java.util.Set<String> extractPlaceholders() {
        var matcher = PLACEHOLDER_PATTERN.matcher(template);
        return matcher.results()
                .map(match -> match.group(1))
                .collect(Collectors.toSet());
    }
}