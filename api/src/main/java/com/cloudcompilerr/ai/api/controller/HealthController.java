package com.cloudcompilerr.ai.api.controller;

import com.cloudcompilerr.ai.rag.service.CircuitBreakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Health and memory monitoring controller with circuit breaker status.
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final CircuitBreakerService circuitBreakerService;

    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        Map<String, Object> memoryInfo = Map.of(
            "maxMemoryMB", maxMemory / (1024 * 1024),
            "totalMemoryMB", totalMemory / (1024 * 1024),
            "usedMemoryMB", usedMemory / (1024 * 1024),
            "freeMemoryMB", freeMemory / (1024 * 1024),
            "usagePercentage", (usedMemory * 100) / maxMemory
        );
        
        log.info("Memory info requested: {}", memoryInfo);
        return ResponseEntity.ok(memoryInfo);
    }
    
    @PostMapping("/gc")
    public ResponseEntity<Map<String, String>> forceGarbageCollection() {
        long beforeGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long afterGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        Map<String, String> result = Map.of(
            "message", "Garbage collection requested",
            "memoryBeforeGC", (beforeGC / (1024 * 1024)) + " MB",
            "memoryAfterGC", (afterGC / (1024 * 1024)) + " MB",
            "memoryFreed", ((beforeGC - afterGC) / (1024 * 1024)) + " MB"
        );
        
        log.info("GC requested: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/circuit-breaker")
    public ResponseEntity<CircuitBreakerService.CircuitBreakerStatus> getCircuitBreakerStatus() {
        CircuitBreakerService.CircuitBreakerStatus status = circuitBreakerService.getStatus();
        log.info("Circuit breaker status requested: {}", status);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/circuit-breaker/reset")
    public ResponseEntity<Map<String, String>> resetCircuitBreaker() {
        circuitBreakerService.reset();
        Map<String, String> result = Map.of(
            "message", "Circuit breaker reset successfully",
            "status", "CLOSED"
        );
        log.info("Circuit breaker reset requested");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        CircuitBreakerService.CircuitBreakerStatus cbStatus = circuitBreakerService.getStatus();
        
        Map<String, Object> systemHealth = Map.of(
            "memory", Map.of(
                "maxMB", runtime.maxMemory() / (1024 * 1024),
                "totalMB", runtime.totalMemory() / (1024 * 1024),
                "usedMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "freeMB", runtime.freeMemory() / (1024 * 1024)
            ),
            "circuitBreaker", Map.of(
                "state", cbStatus.state(),
                "failureCount", cbStatus.failureCount(),
                "requestCount", cbStatus.requestCount()
            ),
            "processors", runtime.availableProcessors(),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(systemHealth);
    }
}