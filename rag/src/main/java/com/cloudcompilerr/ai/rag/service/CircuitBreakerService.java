package com.cloudcompilerr.ai.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker implementation for OpenAI API calls to prevent cascade failures.
 */
@Slf4j
@Service
public class CircuitBreakerService {

    public enum CircuitState {
        CLOSED,    // Normal operation
        OPEN,      // Circuit is open, failing fast
        HALF_OPEN  // Testing if service is back
    }

    // Configuration
    private static final int FAILURE_THRESHOLD = 5;
    private static final int SUCCESS_THRESHOLD = 3;
    private static final long TIMEOUT_DURATION_SECONDS = 60;
    private static final long RATE_LIMIT_WINDOW_SECONDS = 60;
    private static final int MAX_REQUESTS_PER_WINDOW = 100;

    // Circuit breaker state
    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();
    
    // Rate limiting
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicReference<LocalDateTime> windowStart = new AtomicReference<>(LocalDateTime.now());

    /**
     * Executes a function with circuit breaker protection.
     *
     * @param operation The operation to execute
     * @param fallback Fallback operation if circuit is open
     * @return Result of the operation or fallback
     */
    public <T> T execute(CircuitBreakerOperation<T> operation, CircuitBreakerFallback<T> fallback) {
        // Check rate limiting first
        if (!checkRateLimit()) {
            log.warn("Rate limit exceeded, using fallback");
            return fallback.execute("Rate limit exceeded");
        }

        CircuitState currentState = state.get();
        
        switch (currentState) {
            case OPEN:
                if (shouldAttemptReset()) {
                    state.set(CircuitState.HALF_OPEN);
                    log.info("Circuit breaker transitioning to HALF_OPEN");
                    return executeOperation(operation, fallback);
                } else {
                    log.debug("Circuit breaker is OPEN, using fallback");
                    return fallback.execute("Circuit breaker is open");
                }
                
            case HALF_OPEN:
                return executeOperation(operation, fallback);
                
            case CLOSED:
            default:
                return executeOperation(operation, fallback);
        }
    }

    /**
     * Executes the actual operation with error handling.
     */
    private <T> T executeOperation(CircuitBreakerOperation<T> operation, CircuitBreakerFallback<T> fallback) {
        try {
            T result = operation.execute();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            log.error("Operation failed, using fallback: {}", e.getMessage());
            return fallback.execute(e.getMessage());
        }
    }

    /**
     * Handles successful operation.
     */
    private void onSuccess() {
        failureCount.set(0);
        
        if (state.get() == CircuitState.HALF_OPEN) {
            int currentSuccessCount = successCount.incrementAndGet();
            if (currentSuccessCount >= SUCCESS_THRESHOLD) {
                state.set(CircuitState.CLOSED);
                successCount.set(0);
                log.info("Circuit breaker reset to CLOSED after {} successful attempts", SUCCESS_THRESHOLD);
            }
        }
    }

    /**
     * Handles failed operation.
     */
    private void onFailure() {
        lastFailureTime.set(LocalDateTime.now());
        int currentFailureCount = failureCount.incrementAndGet();
        
        if (currentFailureCount >= FAILURE_THRESHOLD) {
            state.set(CircuitState.OPEN);
            log.warn("Circuit breaker opened after {} failures", FAILURE_THRESHOLD);
        }
    }

    /**
     * Checks if circuit breaker should attempt to reset.
     */
    private boolean shouldAttemptReset() {
        LocalDateTime lastFailure = lastFailureTime.get();
        if (lastFailure == null) {
            return true;
        }
        
        return ChronoUnit.SECONDS.between(lastFailure, LocalDateTime.now()) >= TIMEOUT_DURATION_SECONDS;
    }

    /**
     * Checks rate limiting.
     */
    private boolean checkRateLimit() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentWindowStart = windowStart.get();
        
        // Reset window if needed
        if (ChronoUnit.SECONDS.between(currentWindowStart, now) >= RATE_LIMIT_WINDOW_SECONDS) {
            windowStart.set(now);
            requestCount.set(0);
        }
        
        long currentCount = requestCount.incrementAndGet();
        return currentCount <= MAX_REQUESTS_PER_WINDOW;
    }

    /**
     * Gets current circuit breaker status.
     */
    public CircuitBreakerStatus getStatus() {
        return new CircuitBreakerStatus(
            state.get(),
            failureCount.get(),
            successCount.get(),
            lastFailureTime.get(),
            requestCount.get()
        );
    }

    /**
     * Manually resets the circuit breaker.
     */
    public void reset() {
        state.set(CircuitState.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        lastFailureTime.set(null);
        log.info("Circuit breaker manually reset");
    }

    /**
     * Functional interface for operations protected by circuit breaker.
     */
    @FunctionalInterface
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Functional interface for fallback operations.
     */
    @FunctionalInterface
    public interface CircuitBreakerFallback<T> {
        T execute(String reason);
    }

    /**
     * Circuit breaker status information.
     */
    public record CircuitBreakerStatus(
        CircuitState state,
        int failureCount,
        int successCount,
        LocalDateTime lastFailureTime,
        long requestCount
    ) {}
}