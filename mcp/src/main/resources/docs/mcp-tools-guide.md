# MCP Tools Guide

This guide provides detailed information about the Model Context Protocol (MCP) tools available in the Spring AI Showcase application.

## Overview

Model Context Protocol (MCP) tools are functions that can be called by AI models to extend their capabilities beyond text generation. These tools allow the AI to interact with external systems, retrieve information, and perform specialized tasks.

## Available Tools

### Weather Tool (`get_current_weather`)

The Weather Tool provides weather information for a specified location.

#### Parameters

| Parameter | Type   | Required | Description                                      | Example           |
|-----------|--------|----------|--------------------------------------------------|-------------------|
| location  | string | Yes      | The city and state, e.g. San Francisco, CA       | "Boston, MA"      |
| unit      | string | No       | Temperature unit: "celsius" or "fahrenheit"      | "celsius"         |

#### Example Usage

```json
{
  "name": "get_current_weather",
  "arguments": {
    "location": "New York, NY",
    "unit": "fahrenheit"
  }
}
```

#### Example Response

```
The current weather in New York, NY is partly cloudy with a temperature of 72°F. Humidity is at 45% with wind speeds of 8 mph.
```

### Text Analyzer Tool (`analyze_text`)

The Text Analyzer Tool provides various text analysis capabilities including statistics, sentiment analysis, entity extraction, and summarization.

#### Parameters

| Parameter    | Type   | Required | Description                                      | Example           |
|--------------|--------|----------|--------------------------------------------------|-------------------|
| text         | string | Yes      | The text to analyze                              | "Sample text..."  |
| analysisType | string | Yes      | Type of analysis: "statistics", "sentiment", "entities", or "summary" | "sentiment" |

#### Analysis Types

1. **statistics**: Provides text statistics like character count, word count, sentence count, etc.
2. **sentiment**: Analyzes the sentiment of the text (positive, negative, neutral)
3. **entities**: Extracts potential entities like persons, organizations, and locations
4. **summary**: Generates a concise summary of the text

#### Example Usage

```json
{
  "name": "analyze_text",
  "arguments": {
    "text": "Apple Inc. announced their new product yesterday in Cupertino. CEO Tim Cook was very excited about this amazing new device.",
    "analysisType": "entities"
  }
}
```

#### Example Response

```
Entity Extraction:
- Persons: Tim Cook
- Organizations: Apple Inc.
- Locations: Cupertino
```

### File Analyzer Tool (`analyze_file`)

The File Analyzer Tool provides capabilities to analyze files and directories, including file information, directory structure, file content, and statistics.

#### Parameters

| Parameter    | Type   | Required | Description                                      | Example           |
|--------------|--------|----------|--------------------------------------------------|-------------------|
| path         | string | Yes      | The file or directory path to analyze            | "/path/to/file.txt" |
| analysisType | string | Yes      | Type of analysis: "fileInfo", "directoryStructure", "fileContent", or "fileStats" | "fileInfo" |
| maxDepth     | number | No       | Maximum depth for directory traversal (for directoryStructure analysis) | 3 |

#### Analysis Types

1. **fileInfo**: Provides detailed information about a file (size, permissions, timestamps)
2. **directoryStructure**: Shows the structure of a directory up to the specified depth
3. **fileContent**: Analyzes the content of a file (type, statistics, preview)
4. **fileStats**: Provides statistics about a file or directory

#### Example Usage

```json
{
  "name": "analyze_file",
  "arguments": {
    "path": "/home/user/documents",
    "analysisType": "directoryStructure",
    "maxDepth": 2
  }
}
```

#### Example Response

```
Directory Structure for /home/user/documents:
  - report.pdf
  - presentation.pptx
  - images/
    - logo.png
    - banner.jpg
  - data/
    - stats.csv
    - users.json
```

## Implementation Details

### Java 21 Features

The MCP tools leverage modern Java 21 features:

1. **Pattern Matching for Switch**: Used in tool execution logic to handle different analysis types and parameters.
2. **Record Patterns**: Used for structured data handling and parameter validation.
3. **Text Blocks**: Used for formatted multi-line string responses.
4. **Enhanced Stream API**: Used for data processing and transformation.
5. **Sealed Classes**: Used for entity type hierarchies in the text analyzer.

### Error Handling

All tools implement robust error handling:

- Parameter validation with descriptive error messages
- Exception handling with appropriate logging
- Graceful degradation when services are unavailable

### Security Considerations

- File access is restricted to prevent unauthorized access
- Large file handling is limited to prevent resource exhaustion
- Input validation to prevent injection attacks

## Extending MCP Tools

To create a new MCP tool:

1. Create a new class in the `com.example.ai.mcp.tools` package
2. Implement the tool logic and parameter handling
3. Register the tool with the `McpService` using `@PostConstruct`
4. Define the tool parameters and description
5. Update the `McpToolService` to include the new tool

Example template for a new tool:

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class MyNewTool {

    private final McpService mcpService;
    
    @PostConstruct
    public void register() {
        try {
            Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
            
            parameters.put("param1", McpTool.ParameterInfo.builder()
                    .type("string")
                    .description("Description of parameter 1")
                    .required(true)
                    .build());
            
            McpTool myTool = McpTool.builder()
                    .name("my_tool_name")
                    .description("Description of what my tool does")
                    .parameters(parameters)
                    .required(false)
                    .build();
            
            mcpService.registerTool(myTool, this::executeMyTool);
            log.info("My tool registered successfully");
        } catch (Exception e) {
            log.error("Failed to register my tool", e);
        }
    }
    
    private String executeMyTool(Map<String, Object> arguments) {
        // Tool implementation
    }
}
```