package com.example.ai.vectordb.service;

import com.example.ai.core.config.AiProperties;
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

/**
 * Service for vector store operations.
 * This service provides methods for storing and retrieving vectors.
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
     *
     * @return The vector store
     */
    public VectorStore createVectorStore() {
        return new PgVectorStore(jdbcTemplate, embeddingClient, aiProperties.getVectorDb().getDimension());
    }

    /**
     * Adds documents to the vector store.
     *
     * @param documents The documents to add
     */
    public void addDocuments(List<Document> documents) {
        VectorStore vectorStore = createVectorStore();
        vectorStore.add(documents);
        log.info("Added {} documents to vector store", documents.size());
    }

    /**
     * Searches for similar documents in the vector store.
     *
     * @param query The search query
     * @param topK The number of results to return
     * @return List of similar documents
     */
    public List<Document> search(String query, int topK) {
        VectorStore vectorStore = createVectorStore();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(topK)
        );
        log.info("Found {} similar documents for query: {}", results.size(), query);
        return results;
    }

    /**
     * Searches for similar documents in the vector store with filters.
     *
     * @param query The search query
     * @param filters The metadata filters
     * @param topK The number of results to return
     * @return List of similar documents
     */
    public List<Document> search(String query, Map<String, Object> filters, int topK) {
        VectorStore vectorStore = createVectorStore();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(topK).withSimilarityThreshold(0.7f).withFilterExpression(filters)
        );
        log.info("Found {} similar documents for query: {} with filters", results.size(), query);
        return results;
    }

    /**
     * Deletes documents from the vector store.
     *
     * @param ids The document IDs to delete
     */
    public void deleteDocuments(List<String> ids) {
        VectorStore vectorStore = createVectorStore();
        vectorStore.delete(ids);
        log.info("Deleted {} documents from vector store", ids.size());
    }
}