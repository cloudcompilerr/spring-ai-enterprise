package com.example.ai.vectordb.service;

import com.example.ai.core.config.AiProperties;
import com.example.ai.core.config.AiProperties.VectorDbConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Service for vector store operations.
 * This service provides methods for storing and retrieving vectors.
 * Updated to use Java 21 features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingClient embeddingClient;
    private final AiProperties aiProperties;

    /**
     * Creates a vector store instance.
     * Uses Java 21 records for immutable configuration.
     *
     * @return The vector store
     */
    public VectorStore createVectorStore() {
        // Using Java 21 records for immutable configuration
        VectorDbConfig config = aiProperties.getVectorDb().asVectorDbConfig();
        return new PgVectorStore(jdbcTemplate, embeddingClient, config.dimension());
    }

    /**
     * Adds documents to the vector store.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param documents The documents to add
     */
    public void addDocuments(List<Document> documents) {
        // Using Java 21 pattern matching for instanceof with conditional binding
        if (documents instanceof List<Document> docs && !docs.isEmpty()) {
            try (var vectorStore = createVectorStore()) {
                vectorStore.add(docs);
                log.info("Added {} documents to vector store", docs.size());
            } catch (Exception e) {
                log.error("Error adding documents to vector store", e);
                throw new RuntimeException("Failed to add documents to vector store", e);
            }
        } else {
            log.warn("No documents provided for adding to vector store");
        }
    }

    /**
     * Searches for similar documents in the vector store.
     * Uses Java 21 enhanced error handling and functional interfaces.
     *
     * @param query The search query
     * @param topK The number of results to return
     * @return List of similar documents
     */
    public List<Document> search(String query, int topK) {
        // Using Java 21 record for search parameters
        record SearchParams(String query, int topK) {}
        var params = new SearchParams(query, topK);
        
        // Using Java 21 functional interfaces with enhanced error handling
        return executeSearch(() -> 
            SearchRequest.query(params.query()).withTopK(params.topK()),
            "Found {} similar documents for query: " + params.query()
        );
    }

    /**
     * Searches for similar documents in the vector store with filters.
     * Uses Java 21 enhanced error handling and functional interfaces.
     *
     * @param query The search query
     * @param filters The metadata filters
     * @param topK The number of results to return
     * @return List of similar documents
     */
    public List<Document> search(String query, Map<String, Object> filters, int topK) {
        // Using Java 21 record for search parameters with filters
        record FilteredSearchParams(String query, Map<String, Object> filters, int topK) {}
        var params = new FilteredSearchParams(query, filters, topK);
        
        // Using Java 21 functional interfaces with enhanced error handling
        return executeSearch(() -> 
            SearchRequest.query(params.query())
                .withTopK(params.topK())
                .withSimilarityThreshold(0.7f)
                .withFilterExpression(params.filters()),
            "Found {} similar documents for query: " + params.query() + " with filters"
        );
    }
    
    /**
     * Executes a search with the given request supplier.
     * Uses Java 21 functional programming and enhanced error handling.
     *
     * @param requestSupplier Supplier for the search request
     * @param logMessage Log message template
     * @return List of similar documents
     */
    private List<Document> executeSearch(Supplier<SearchRequest> requestSupplier, String logMessage) {
        try (var vectorStore = createVectorStore()) {
            var results = vectorStore.similaritySearch(requestSupplier.get());
            log.info(logMessage, results.size());
            return results;
        } catch (Exception e) {
            log.error("Error searching vector store", e);
            throw new RuntimeException("Failed to search vector store", e);
        }
    }

    /**
     * Deletes documents from the vector store.
     * Uses Java 21 pattern matching and enhanced error handling.
     *
     * @param ids The document IDs to delete
     */
    public void deleteDocuments(List<String> ids) {
        // Using Java 21 pattern matching for instanceof with conditional binding
        if (ids instanceof List<String> docIds && !docIds.isEmpty()) {
            try (var vectorStore = createVectorStore()) {
                vectorStore.delete(docIds);
                log.info("Deleted {} documents from vector store", docIds.size());
            } catch (Exception e) {
                log.error("Error deleting documents from vector store", e);
                throw new RuntimeException("Failed to delete documents from vector store", e);
            }
        } else {
            log.warn("No document IDs provided for deletion from vector store");
        }
    }
}