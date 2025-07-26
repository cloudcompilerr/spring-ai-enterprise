package com.cloudcompilerr.ai.mcp.tools;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MCP tool for analyzing files and directories using Java 21 features.
 * This tool provides capabilities to analyze file content, directory structure,
 * and file metadata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileAnalyzerTool {

    private final McpService mcpService;
    
    /**
     * Registers the file analyzer tool with the MCP service.
     */
    @PostConstruct
    public void register() {
        try {
            Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
            
            parameters.put("path", McpTool.ParameterInfo.builder()
                    .type("string")
                    .description("The file or directory path to analyze")
                    .required(true)
                    .build());
            
            parameters.put("analysisType", McpTool.ParameterInfo.builder()
                    .type("string")
                    .description("The type of analysis to perform")
                    .required(true)
                    .enumValues(List.of("fileInfo", "directoryStructure", "fileContent", "fileStats"))
                    .build());
            
            parameters.put("maxDepth", McpTool.ParameterInfo.builder()
                    .type("number")
                    .description("Maximum depth for directory traversal (for directoryStructure analysis)")
                    .required(false)
                    .build());
            
            McpTool fileAnalyzerTool = McpTool.builder()
                    .name("analyze_file")
                    .description("Analyze files and directories for information, structure, content, or statistics")
                    .parameters(parameters)
                    .required(false)
                    .build();
            
            mcpService.registerTool(fileAnalyzerTool, this::executeFileAnalyzerTool);
            log.info("File analyzer tool registered successfully");
        } catch (Exception e) {
            log.error("Failed to register file analyzer tool", e);
        }
    }

    /**
     * Executes the file analyzer tool.
     *
     * @param arguments The arguments for the tool
     * @return The analysis result
     */
    private String executeFileAnalyzerTool(Map<String, Object> arguments) {
        try {
            // Validate arguments
            if (arguments == null || !arguments.containsKey("path") || !arguments.containsKey("analysisType")) {
                return "Error: Both path and analysisType are required";
            }
            
            String pathStr = (String) arguments.get("path");
            if (pathStr == null || pathStr.trim().isEmpty()) {
                return "Error: Path cannot be empty";
            }
            
            String analysisType = (String) arguments.get("analysisType");
            if (analysisType == null || analysisType.trim().isEmpty()) {
                return "Error: Analysis type cannot be empty";
            }
            
            // Parse maxDepth if provided
            Integer maxDepth = null;
            if (arguments.containsKey("maxDepth")) {
                Object maxDepthObj = arguments.get("maxDepth");
                if (maxDepthObj instanceof Number) {
                    maxDepth = ((Number) maxDepthObj).intValue();
                } else if (maxDepthObj instanceof String) {
                    try {
                        maxDepth = Integer.parseInt((String) maxDepthObj);
                    } catch (NumberFormatException e) {
                        return "Error: maxDepth must be a valid number";
                    }
                }
            }
            
            // Resolve path
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                return "Error: Path does not exist: " + pathStr;
            }
            
            // Using Java 21 switch expressions with pattern matching
            return switch (analysisType.toLowerCase()) {
                case "fileinfo" -> analyzeFileInfo(path);
                case "directorystructure" -> analyzeDirectoryStructure(path, maxDepth != null ? maxDepth : 2);
                case "filecontent" -> analyzeFileContent(path);
                case "filestats" -> analyzeFileStats(path);
                default -> "Error: Unknown analysis type. Supported types are: fileInfo, directoryStructure, fileContent, fileStats";
            };
            
        } catch (Exception e) {
            log.error("Error executing file analyzer tool", e);
            return "Error analyzing file: " + e.getMessage();
        }
    }
    
    /**
     * Analyzes file information using Java 21 features.
     * 
     * @param path The file path
     * @return Information about the file
     */
    private String analyzeFileInfo(Path path) throws IOException {
        // Using Java 21 enhanced record patterns
        record FileAttributes(
            boolean isDirectory,
            boolean isRegularFile,
            boolean isSymbolicLink,
            boolean isHidden,
            boolean isReadable,
            boolean isWritable,
            boolean isExecutable,
            long size,
            String owner,
            String lastModified,
            String creationTime,
            String lastAccessTime
        ) {}
        
        var attributes = Files.readAttributes(path, "posix:*");
        
        // Using Java 21 record pattern in variable declarations
        var fileAttributes = new FileAttributes(
            Files.isDirectory(path),
            Files.isRegularFile(path),
            Files.isSymbolicLink(path),
            Files.isHidden(path),
            Files.isReadable(path),
            Files.isWritable(path),
            Files.isExecutable(path),
            Files.size(path),
            attributes.get("owner").toString(),
            attributes.get("lastModifiedTime").toString(),
            attributes.get("creationTime").toString(),
            attributes.get("lastAccessTime").toString()
        );
        
        // Using Java 21 text blocks with formatting
        return """
               File Information for %s:
               - Type: %s
               - Size: %d bytes
               - Owner: %s
               - Permissions: %s%s%s
               - Last Modified: %s
               - Creation Time: %s
               - Last Access: %s
               """.formatted(
                   path.toString(),
                   fileAttributes.isDirectory() ? "Directory" : 
                   (fileAttributes.isSymbolicLink() ? "Symbolic Link" : "Regular File"),
                   fileAttributes.size(),
                   fileAttributes.owner(),
                   fileAttributes.isReadable() ? "r" : "-",
                   fileAttributes.isWritable() ? "w" : "-",
                   fileAttributes.isExecutable() ? "x" : "-",
                   fileAttributes.lastModified(),
                   fileAttributes.creationTime(),
                   fileAttributes.lastAccessTime()
               );
    }
    
    /**
     * Analyzes directory structure using Java 21 features.
     * 
     * @param path The directory path
     * @param maxDepth Maximum depth for directory traversal
     * @return Structure of the directory
     */
    private String analyzeDirectoryStructure(Path path, int maxDepth) throws IOException {
        if (!Files.isDirectory(path)) {
            return "Error: Path is not a directory: " + path;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Directory Structure for ").append(path).append(":\n");
        
        // Using Java 21 try-with-resources enhancements
        try (var stream = Files.walk(path, maxDepth)) {
            // Using Java 21 Stream API enhancements
            var structure = stream
                .filter(p -> !p.equals(path))
                .map(p -> {
                    int depth = path.relativize(p).getNameCount();
                    String indent = "  ".repeat(depth);
                    String name = p.getFileName().toString();
                    String type = Files.isDirectory(p) ? "/" : "";
                    return indent + "- " + name + type;
                })
                .collect(Collectors.joining("\n"));
            
            result.append(structure);
        }
        
        return result.toString();
    }
    
    /**
     * Analyzes file content using Java 21 features.
     * 
     * @param path The file path
     * @return Analysis of the file content
     */
    private String analyzeFileContent(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return "Error: Path is not a regular file: " + path;
        }
        
        // Check file size to avoid loading very large files
        if (Files.size(path) > 1_000_000) { // 1MB limit
            return "Error: File is too large for content analysis (> 1MB)";
        }
        
        // Using Java 21 enhanced try-with-resources
        try (var lines = Files.lines(path)) {
            // Using Java 21 Stream API enhancements
            var lineCount = lines.count();
            
            // Read file content again (since stream was consumed)
            String content = Files.readString(path);
            
            // Detect file type based on content and extension
            String fileType = detectFileType(path, content);
            
            // Calculate basic statistics
            int charCount = content.length();
            int wordCount = content.split("\\s+").length;
            
            // Using Java 21 text blocks with formatting
            return """
                   File Content Analysis for %s:
                   - File Type: %s
                   - Line Count: %d
                   - Character Count: %d
                   - Word Count: %d
                   - Preview (first 200 chars):
                   ---
                   %s
                   ---
                   """.formatted(
                       path.toString(),
                       fileType,
                       lineCount,
                       charCount,
                       wordCount,
                       content.length() > 200 ? content.substring(0, 200) + "..." : content
                   );
        }
    }
    
    /**
     * Detects file type based on path and content.
     * 
     * @param path The file path
     * @param content The file content
     * @return Detected file type
     */
    private String detectFileType(Path path, String content) {
        // Using Java 21 enhanced switch expressions
        String fileName = path.getFileName().toString().toLowerCase();
        String extension = fileName.contains(".") ? 
                fileName.substring(fileName.lastIndexOf('.') + 1) : "";
        
        // Using Java 21 switch expressions with pattern matching
        return switch (extension) {
            case "java" -> "Java Source Code";
            case "py" -> "Python Source Code";
            case "js" -> "JavaScript Source Code";
            case "html" -> "HTML Document";
            case "css" -> "CSS Stylesheet";
            case "json" -> "JSON Data";
            case "xml" -> "XML Document";
            case "md" -> "Markdown Document";
            case "txt" -> "Plain Text";
            case "csv" -> "CSV Data";
            case "pdf" -> "PDF Document";
            case "jpg", "jpeg", "png", "gif" -> "Image File";
            case "mp3", "wav" -> "Audio File";
            case "mp4", "avi", "mov" -> "Video File";
            default -> {
                // Try to detect based on content
                if (content.startsWith("<?xml")) yield "XML Document";
                if (content.startsWith("{") && content.endsWith("}")) yield "JSON Data";
                if (content.contains("<!DOCTYPE html>") || content.contains("<html>")) yield "HTML Document";
                if (content.contains("import java.") || content.contains("public class")) yield "Java Source Code";
                if (content.contains("def ") && content.contains(":")) yield "Python Source Code";
                if (content.contains("function") && content.contains(";")) yield "JavaScript Source Code";
                
                yield "Unknown";
            }
        };
    }
    
    /**
     * Analyzes file statistics using Java 21 features.
     * 
     * @param path The file or directory path
     * @return Statistics about the file or directory
     */
    private String analyzeFileStats(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            // Directory statistics
            try (var files = Files.walk(path)) {
                // Using Java 21 Stream API enhancements with collectors
                var stats = files
                    .filter(Files::isRegularFile)
                    .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.summingLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0L;
                            }
                        }),
                        (count, totalSize) -> Map.of("count", count, "totalSize", totalSize)
                    ));
                
                long fileCount = (Long) stats.get("count");
                long totalSize = (Long) stats.get("totalSize");
                
                // Count directories
                long dirCount;
                try (var dirs = Files.walk(path)) {
                    dirCount = dirs
                        .filter(Files::isDirectory)
                        .count() - 1; // Subtract 1 to exclude the root directory
                }
                
                // Using Java 21 text blocks with formatting
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
            }
        } else {
            // Single file statistics
            long size = Files.size(path);
            String content = Files.readString(path);
            
            int lineCount = content.split("\n").length;
            int wordCount = content.split("\\s+").length;
            int charCount = content.length();
            
            // Calculate character frequency
            var charFrequency = content.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(
                    c -> c,
                    Collectors.counting()
                ));
            
            // Find top 5 most frequent characters
            var topChars = charFrequency.entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> "'" + (e.getKey() == ' ' ? "SPACE" : e.getKey()) + "': " + e.getValue())
                .collect(Collectors.joining(", "));
            
            // Using Java 21 text blocks with formatting
            return """
                   File Statistics for %s:
                   - Size: %d bytes
                   - Lines: %d
                   - Words: %d
                   - Characters: %d
                   - Characters per Line: %.2f
                   - Words per Line: %.2f
                   - Top 5 Characters: %s
                   """.formatted(
                       path.toString(),
                       size,
                       lineCount,
                       wordCount,
                       charCount,
                       lineCount > 0 ? (double) charCount / lineCount : 0,
                       lineCount > 0 ? (double) wordCount / lineCount : 0,
                       topChars
                   );
        }
    }
}