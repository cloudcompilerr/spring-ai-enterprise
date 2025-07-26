package com.cloudcompilerr.ai.rag.repository;

import com.cloudcompilerr.ai.rag.model.Document;
import com.cloudcompilerr.ai.rag.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DocumentChunk entities.
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    /**
     * Find chunks by document.
     *
     * @param document The document to find chunks for
     * @return List of document chunks
     */
    List<DocumentChunk> findByDocument(Document document);

    /**
     * Find chunks by document ordered by chunk index.
     *
     * @param document The document to find chunks for
     * @return List of document chunks ordered by chunk index
     */
    List<DocumentChunk> findByDocumentOrderByChunkIndex(Document document);

    /**
     * Find similar chunks based on vector similarity.
     *
     * @param embeddingVector The embedding vector to compare against
     * @param limit The maximum number of results to return
     * @return List of similar document chunks
     */
    @Query(value = "SELECT dc.* FROM document_chunks dc " +
            "ORDER BY dc.embedding_vector <-> :embeddingVector LIMIT :limit", 
            nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("embeddingVector") float[] embeddingVector, 
            @Param("limit") int limit);

    /**
     * Find similar chunks based on vector similarity with a minimum similarity threshold.
     *
     * @param embeddingVector The embedding vector to compare against
     * @param threshold The similarity threshold (lower values mean more similar)
     * @param limit The maximum number of results to return
     * @return List of similar document chunks
     */
    @Query(value = "SELECT dc.* FROM document_chunks dc " +
            "WHERE dc.embedding_vector <-> :embeddingVector < :threshold " +
            "ORDER BY dc.embedding_vector <-> :embeddingVector LIMIT :limit", 
            nativeQuery = true)
    List<DocumentChunk> findSimilarChunksWithThreshold(
            @Param("embeddingVector") float[] embeddingVector, 
            @Param("threshold") float threshold,
            @Param("limit") int limit);
}