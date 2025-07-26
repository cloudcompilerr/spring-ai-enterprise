package com.cloudcompilerr.ai.mcp.tools;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MCP tool for analyzing text using Java 21 features.
 * This tool provides various text analysis capabilities like sentiment analysis,
 * entity extraction, and text statistics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextAnalyzerTool {

    private final McpService mcpService;
    
    // Using Java 21 Pattern.compile with MULTILINE flag
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]\\s+", Pattern.MULTILINE);
    
    /**
     * Registers the text analyzer tool with the MCP service.
     */
    @PostConstruct
    public void register() {
        try {
            Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
            
            parameters.put("text", McpTool.ParameterInfo.builder()
                    .type("string")
                    .description("The text to analyze")
                    .required(true)
                    .build());
            
            parameters.put("analysisType", McpTool.ParameterInfo.builder()
                    .type("string")
                    .description("The type of analysis to perform")
                    .required(true)
                    .enumValues(List.of("statistics", "sentiment", "entities", "summary"))
                    .build());
            
            McpTool textAnalyzerTool = McpTool.builder()
                    .name("analyze_text")
                    .description("Analyze text for statistics, sentiment, entities, or generate a summary")
                    .parameters(parameters)
                    .required(false)
                    .build();
            
            mcpService.registerTool(textAnalyzerTool, this::executeTextAnalyzerTool);
            log.info("Text analyzer tool registered successfully");
        } catch (Exception e) {
            log.error("Failed to register text analyzer tool", e);
        }
    }

    /**
     * Executes the text analyzer tool.
     *
     * @param arguments The arguments for the tool
     * @return The analysis result
     */
    private String executeTextAnalyzerTool(Map<String, Object> arguments) {
        try {
            // Validate arguments
            if (arguments == null || !arguments.containsKey("text") || !arguments.containsKey("analysisType")) {
                return "Error: Both text and analysisType are required";
            }
            
            String text = (String) arguments.get("text");
            if (text == null || text.trim().isEmpty()) {
                return "Error: Text cannot be empty";
            }
            
            String analysisType = (String) arguments.get("analysisType");
            if (analysisType == null || analysisType.trim().isEmpty()) {
                return "Error: Analysis type cannot be empty";
            }
            
            // Using Java 21 switch expressions with pattern matching
            return switch (analysisType.toLowerCase()) {
                case "statistics" -> analyzeStatistics(text);
                case "sentiment" -> analyzeSentiment(text);
                case "entities" -> extractEntities(text);
                case "summary" -> generateSummary(text);
                default -> "Error: Unknown analysis type. Supported types are: statistics, sentiment, entities, summary";
            };
            
        } catch (Exception e) {
            log.error("Error executing text analyzer tool", e);
            return "Error analyzing text: " + e.getMessage();
        }
    }
    
    /**
     * Analyzes text statistics using Java 21 features.
     * 
     * @param text The text to analyze
     * @return Statistics about the text
     */
    private String analyzeStatistics(String text) {
        // Using Java 21 String templates (preview feature)
        // String template = STR."Text length: \{text.length()} characters";
        
        // Using Java 21 multiline strings and enhanced switch expressions
        var wordCount = text.split("\\s+").length;
        var charCount = text.length();
        var charCountNoSpaces = text.replaceAll("\\s+", "").length();
        
        // Determine complexity based on word count
        String complexity;
        if (wordCount < 50) {
            complexity = "Simple";
        } else if (wordCount < 200) {
            complexity = "Moderate";
        } else if (wordCount < 500) {
            complexity = "Complex";
        } else {
            complexity = "Very Complex";
        }
        
        // Using Java 21 streams with enhanced features
        var sentences = SENTENCE_PATTERN.split(text);
        var sentenceCount = sentences.length;
        
        // Calculate average sentence length using streams
        var avgSentenceLength = sentences.length > 0 ? 
                Math.round((double) wordCount / sentenceCount * 10) / 10.0 : 0;
        
        return """
               Text Statistics:
               - Character count: %d
               - Character count (no spaces): %d
               - Word count: %d
               - Sentence count: %d
               - Average words per sentence: %.1f
               - Complexity: %s
               """.formatted(charCount, charCountNoSpaces, wordCount, sentenceCount, avgSentenceLength, complexity);
    }
    
    /**
     * Performs basic sentiment analysis on text.
     * 
     * @param text The text to analyze
     * @return Sentiment analysis result
     */
    private String analyzeSentiment(String text) {
        // Simple sentiment analysis using predefined word lists
        // In a real implementation, this would use a more sophisticated model
        
        // Using Java 21 record patterns in enhanced for loops
        record WordSentiment(String word, double score) {}
        
        var positiveWords = List.of(
            new WordSentiment("good", 0.8),
            new WordSentiment("great", 0.9),
            new WordSentiment("excellent", 1.0),
            new WordSentiment("happy", 0.8),
            new WordSentiment("love", 0.9),
            new WordSentiment("wonderful", 0.9),
            new WordSentiment("best", 0.8),
            new WordSentiment("amazing", 0.9)
        );
        
        var negativeWords = List.of(
            new WordSentiment("bad", -0.8),
            new WordSentiment("terrible", -0.9),
            new WordSentiment("awful", -1.0),
            new WordSentiment("sad", -0.7),
            new WordSentiment("hate", -0.9),
            new WordSentiment("worst", -0.9),
            new WordSentiment("poor", -0.6),
            new WordSentiment("horrible", -0.9)
        );
        
        // Using Java 21 enhanced pattern matching in instanceof
        double sentimentScore = 0;
        int matchCount = 0;
        
        // Normalize text for comparison
        String normalizedText = text.toLowerCase();
        
        // Calculate sentiment score
        for (var sentiment : positiveWords) {
            if (normalizedText.contains(sentiment.word())) {
                sentimentScore += sentiment.score();
                matchCount++;
            }
        }
        
        for (var sentiment : negativeWords) {
            if (normalizedText.contains(sentiment.word())) {
                sentimentScore += sentiment.score();
                matchCount++;
            }
        }
        
        // Normalize score
        double finalScore = matchCount > 0 ? sentimentScore / matchCount : 0;
        
        // Determine sentiment based on score
        String sentiment;
        int scoreInt = (int) Math.round(finalScore * 10);
        if (scoreInt < -7) {
            sentiment = "Very Negative";
        } else if (scoreInt < -3) {
            sentiment = "Negative";
        } else if (scoreInt < 3) {
            sentiment = "Neutral";
        } else if (scoreInt < 7) {
            sentiment = "Positive";
        } else {
            sentiment = "Very Positive";
        }
        
        return """
               Sentiment Analysis:
               - Overall sentiment: %s
               - Sentiment score: %.2f (range: -1.0 to 1.0)
               - Confidence: %s
               """.formatted(
                   sentiment, 
                   finalScore,
                   matchCount > 5 ? "High" : (matchCount > 2 ? "Medium" : "Low")
               );
    }
    
    /**
     * Extracts potential entities from text.
     * 
     * @param text The text to analyze
     * @return Extracted entities
     */
    private String extractEntities(String text) {
        // Simple entity extraction using regex patterns
        // In a real implementation, this would use NLP models
        
        // Simple entity extraction using basic patterns
        // Using records for entity representation
        
        // Simple patterns for demonstration
        Pattern personPattern = Pattern.compile("\\b[A-Z][a-z]+ [A-Z][a-z]+\\b");
        Pattern orgPattern = Pattern.compile("\\b[A-Z][a-z]* (Inc|Corp|LLC|Company|Organization)\\b");
        Pattern locationPattern = Pattern.compile("\\b(New York|London|Paris|Tokyo|Berlin|Sydney)\\b");
        
        Matcher personMatcher = personPattern.matcher(text);
        Matcher orgMatcher = orgPattern.matcher(text);
        Matcher locationMatcher = locationPattern.matcher(text);
        
        List<String> persons = new ArrayList<>();
        List<String> organizations = new ArrayList<>();
        List<String> locations = new ArrayList<>();
        
        while (personMatcher.find()) {
            persons.add(personMatcher.group());
        }
        
        while (orgMatcher.find()) {
            organizations.add(orgMatcher.group());
        }
        
        while (locationMatcher.find()) {
            locations.add(locationMatcher.group());
        }
        
        return """
               Entity Extraction:
               - Persons: %s
               - Organizations: %s
               - Locations: %s
               """.formatted(
                   persons.isEmpty() ? "None detected" : String.join(", ", persons),
                   organizations.isEmpty() ? "None detected" : String.join(", ", organizations),
                   locations.isEmpty() ? "None detected" : String.join(", ", locations)
               );
    }
    
    /**
     * Generates a simple summary of the text.
     * 
     * @param text The text to summarize
     * @return A summary of the text
     */
    private String generateSummary(String text) {
        // Simple extractive summarization
        // In a real implementation, this would use more sophisticated NLP techniques
        
        // Split text into sentences
        var sentences = SENTENCE_PATTERN.split(text);
        
        // If text is short, return as is
        if (sentences.length <= 3) {
            return "Summary: " + text;
        }
        
        // Using Java 21 enhanced for loops with pattern matching
        record SentenceScore(String sentence, double score) {}
        
        var scoredSentences = new java.util.ArrayList<SentenceScore>();
        
        // Simple scoring based on sentence position and length
        for (int i = 0; i < sentences.length; i++) {
            var sentence = sentences[i].trim();
            
            // Skip empty sentences
            if (sentence.isEmpty()) continue;
            
            // Score based on position (first and last sentences often contain key information)
            double positionScore = 0;
            if (i == 0) positionScore = 1.0;
            else if (i == sentences.length - 1) positionScore = 0.8;
            else positionScore = 0.5;
            
            // Score based on length (not too short, not too long)
            String[] words = sentence.split("\\s+");
            double lengthScore;
            int wordCount = words.length;
            if (wordCount < 5) {
                lengthScore = 0.3;
            } else if (wordCount < 10) {
                lengthScore = 0.7;
            } else if (wordCount < 20) {
                lengthScore = 1.0;
            } else if (wordCount < 30) {
                lengthScore = 0.7;
            } else {
                lengthScore = 0.4;
            }
            
            // Combined score
            double score = (positionScore + lengthScore) / 2.0;
            scoredSentences.add(new SentenceScore(sentence, score));
        }
        
        // Sort by score and take top sentences
        var topSentences = scoredSentences.stream()
                .sorted((s1, s2) -> Double.compare(s2.score(), s1.score()))
                .limit(Math.min(3, sentences.length))
                .map(SentenceScore::sentence)
                .collect(Collectors.toList());
        
        return "Summary: " + String.join(" ", topSentences);
    }
}