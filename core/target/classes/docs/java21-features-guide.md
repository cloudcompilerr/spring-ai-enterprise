# Java 21 Features Guide

This guide provides detailed information about the Java 21 features used in the Spring AI Showcase application.

## Overview

Java 21 is a Long-Term Support (LTS) release that introduces several powerful features to enhance developer productivity, code readability, and application performance. This project leverages these features to create more maintainable, concise, and efficient code.

## Key Java 21 Features Used

### 1. Pattern Matching for Switch

Pattern matching for switch expressions allows for more powerful and expressive switch statements that can match against patterns rather than just constants.

#### Example from our codebase:

```java
// From TextAnalyzerTool.java
return switch (analysisType.toLowerCase()) {
    case "statistics" -> analyzeStatistics(text);
    case "sentiment" -> analyzeSentiment(text);
    case "entities" -> extractEntities(text);
    case "summary" -> generateSummary(text);
    default -> "Error: Unknown analysis type. Supported types are: statistics, sentiment, entities, summary";
};
```

#### Benefits:
- More concise and readable code
- Eliminates the need for break statements
- Supports complex pattern matching with guards

### 2. Record Patterns

Record patterns allow for destructuring records in pattern matching contexts, making it easier to work with structured data.

#### Example from our codebase:

```java
// From ChatServiceTest.java
@ParameterizedTest
@MethodSource("templateTestCases")
void shouldGenerateTemplatedResponse(TemplateTestCase testCase) {
    // Using Java 21 record pattern in method parameter
    var (templateName, variables, expectedTemplate) = testCase;
    
    // Test implementation...
}

// Record definition
record TemplateTestCase(String templateName, Map<String, String> variables, PromptTemplate expectedTemplate) {}
```

#### Benefits:
- Concise data extraction from records
- Improved readability for data-oriented code
- Reduced boilerplate for accessing record components

### 3. Text Blocks

Text blocks provide a way to include multi-line string literals in Java code, making it easier to work with formatted text.

#### Example from our codebase:

```java
// From FileAnalyzerTool.java
return """
       Directory Statistics for %s:
       - Total Files: %d
       - Total Subdirectories: %d
       - Total Size: %d bytes (%.2f MB)
       - Average File Size: %.2f bytes
       """.formatted(
           path.toString(),
           fileCount,
           dirCount,
           totalSize,
           totalSize / (1024.0 * 1024.0),
           fileCount > 0 ? (double) totalSize / fileCount : 0
       );
```

#### Benefits:
- Improved readability for multi-line strings
- Preserves formatting and indentation
- Eliminates the need for string concatenation
- Works well with string formatting methods

### 4. Enhanced Switch Expressions

Switch expressions allow switches to be used as expressions rather than statements, and they can return values.

#### Example from our codebase:

```java
// From ChatService.java
PromptTemplate systemPrompt = switch (templateName) {
    case "code" -> SystemPromptTemplates.CODE_GENERATION;
    case "data" -> SystemPromptTemplates.DATA_ANALYSIS;
    case "summary" -> SystemPromptTemplates.SUMMARIZATION;
    case "qa" -> SystemPromptTemplates.QUESTION_ANSWERING;
    default -> SystemPromptTemplates.GENERAL_PURPOSE;
};
```

#### Benefits:
- More concise code
- Expressions can be used in assignments
- Exhaustiveness checking by the compiler

### 5. Pattern Matching for instanceof

Pattern matching for instanceof allows for type checking and casting in a single operation, reducing boilerplate code.

#### Example from our codebase:

```java
// From ChatService.java
String effectiveSystemPrompt = systemPrompt instanceof String s && !s.isEmpty() 
        ? s 
        : "You are a helpful assistant with access to tools. Use the tools when appropriate to answer the user's question.";
```

#### Benefits:
- Eliminates explicit casting after instanceof checks
- Reduces boilerplate code
- Improves readability

### 6. Records

Records are immutable data classes that require minimal boilerplate code, automatically providing constructors, accessors, equals, hashCode, and toString methods.

#### Example from our codebase:

```java
// From TextAnalyzerTool.java
record WordSentiment(String word, double score) {}

// Usage
var positiveWords = List.of(
    new WordSentiment("good", 0.8),
    new WordSentiment("great", 0.9),
    // ...
);
```

#### Benefits:
- Reduced boilerplate for data carrier classes
- Immutability by default
- Automatic implementation of common methods
- Clear indication of the class's purpose

### 7. Sealed Classes

Sealed classes restrict which other classes may extend or implement them, providing more control over class hierarchies.

#### Example from our codebase:

```java
// From TextAnalyzerTool.java
sealed interface Entity permits PersonEntity, OrganizationEntity, LocationEntity {}
record PersonEntity(String name) implements Entity {}
record OrganizationEntity(String name) implements Entity {}
record LocationEntity(String name) implements Entity {}
```

#### Benefits:
- Explicit control over class hierarchies
- Better documentation of design intent
- Enables exhaustive pattern matching
- Improved type safety

### 8. Enhanced Stream API

Java 21 includes enhancements to the Stream API, making it more powerful and expressive.

#### Example from our codebase:

```java
// From ChatService.java
List<McpTool> tools = toolNames.stream()
        .map(mcpToolService::getToolByName)
        .filter(tool -> tool != null)
        .toList(); // Java 21 immutable list collector
```

#### Benefits:
- More concise stream operations
- Improved performance
- New collectors for common operations
- Better support for immutable collections

### 9. Virtual Threads

Virtual threads are lightweight threads that dramatically reduce the effort of writing, maintaining, and observing high-throughput concurrent applications.

#### Example from our codebase:

```java
// Not explicitly shown in the code snippets, but can be used for handling concurrent requests
// in the Spring Boot application
```

#### Benefits:
- Dramatically improved scalability for I/O-bound applications
- Reduced memory footprint
- Simplified concurrent programming model
- Better CPU utilization

## Best Practices for Using Java 21 Features

1. **Use Pattern Matching Judiciously**: Pattern matching can make code more concise, but overuse can reduce readability. Use it when it genuinely simplifies the code.

2. **Prefer Records for Data Transfer Objects**: Records are ideal for DTOs, value objects, and other immutable data carriers.

3. **Use Text Blocks for Multi-line Strings**: Text blocks improve readability for SQL queries, HTML templates, JSON structures, and other multi-line text.

4. **Leverage Switch Expressions for Complex Conditionals**: Switch expressions are more concise and safer than traditional switch statements, especially for complex conditional logic.

5. **Consider Sealed Classes for Domain Modeling**: Sealed classes provide better control over class hierarchies and are particularly useful for domain modeling.

6. **Use Virtual Threads for I/O-Bound Operations**: Virtual threads excel at handling I/O-bound operations with minimal resource usage.

## Migration Tips

When migrating existing code to use Java 21 features:

1. **Start with Low-Risk Areas**: Begin with isolated components or utility classes.

2. **Replace Boilerplate Data Classes with Records**: Look for simple data classes that can be converted to records.

3. **Convert Multi-line String Concatenation to Text Blocks**: Improve readability by replacing string concatenation with text blocks.

4. **Refactor Complex if-else Chains to Switch Expressions**: Switch expressions can make complex conditional logic more maintainable.

5. **Add Pattern Matching to Existing instanceof Checks**: This is a simple change that reduces boilerplate without changing behavior.

## Conclusion

Java 21 features enable more concise, readable, and maintainable code. By leveraging these features in the Spring AI Showcase application, we've created a modern codebase that demonstrates best practices for Java development.

For more information about Java 21 features, refer to the [official Java documentation](https://docs.oracle.com/en/java/javase/21/language/java-language-changes.html).