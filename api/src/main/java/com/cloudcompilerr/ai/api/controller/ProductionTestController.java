package com.cloudcompilerr.ai.api.controller;

import com.cloudcompilerr.ai.api.dto.DocumentRequest;
import com.cloudcompilerr.ai.api.dto.DocumentResponse;
import com.cloudcompilerr.ai.rag.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Production test controller for testing new features.
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class ProductionTestController {

    private final DocumentService documentService;

    @PostMapping("/document/async")
    public CompletableFuture<ResponseEntity<DocumentResponse>> createDocumentAsync(
            @RequestBody DocumentRequest request) {
        
        log.info("Async document creation requested: {}", request.title());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                var document = documentService.createDocument(
                    request.title(),
                    request.content(),
                    request.sourceUrl(),
                    request.documentType()
                );
                
                var response = new DocumentResponse(
                    document.getId(),
                    document.getTitle(),
                    document.getContent(),
                    document.getSourceUrl(),
                    document.getDocumentType(),
                    document.getCreatedAt(),
                    document.getUpdatedAt()
                );
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                log.error("Async document creation failed: {}", e.getMessage());
                throw new RuntimeException("Async document creation failed", e);
            }
        });
    }

    @PostMapping("/document/large")
    public ResponseEntity<Map<String, Object>> testLargeDocument(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String baseContent = request.get("content");
        int multiplier = Integer.parseInt(request.getOrDefault("multiplier", "10"));
        
        // Create large content by repeating base content
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < multiplier; i++) {
            largeContent.append(baseContent).append(" [Iteration ").append(i + 1).append("] ");
        }
        
        log.info("Testing large document: {} characters", largeContent.length());
        
        try {
            long startTime = System.currentTimeMillis();
            
            var document = documentService.createDocument(
                title,
                largeContent.toString(),
                "test://large-document-" + System.currentTimeMillis(),
                "text"
            );
            
            long endTime = System.currentTimeMillis();
            
            Map<String, Object> result = Map.of(
                "success", true,
                "documentId", document.getId(),
                "contentLength", largeContent.length(),
                "processingTimeMs", endTime - startTime,
                "message", "Large document processed successfully"
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Large document test failed: {}", e.getMessage());
            
            Map<String, Object> result = Map.of(
                "success", false,
                "error", e.getMessage(),
                "contentLength", largeContent.length(),
                "message", "Large document processing failed"
            );
            
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/stress/{count}")
    public ResponseEntity<Map<String, Object>> stressTest(@PathVariable int count) {
        log.info("Starting stress test with {} documents", count);
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        
        for (int i = 0; i < count; i++) {
            try {
                documentService.createDocument(
                    "Stress Test Document " + i,
                    "This is stress test content for document number " + i + ". " +
                    "It contains some text to test the system under load.",
                    "test://stress-" + i,
                    "text"
                );
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.warn("Stress test document {} failed: {}", i, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = Map.of(
            "totalDocuments", count,
            "successCount", successCount,
            "failureCount", failureCount,
            "totalTimeMs", endTime - startTime,
            "averageTimePerDocMs", (endTime - startTime) / count,
            "successRate", (double) successCount / count * 100
        );
        
        log.info("Stress test completed: {}", result);
        return ResponseEntity.ok(result);
    }
}