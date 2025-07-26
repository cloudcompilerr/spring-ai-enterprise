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
     * Utility method to convert float array to PostgreSQL vector format.
     * 
     * @param vector The float array to convert
     * @return String representation in vector format [1.0,2.0,3.0]
     */
    default String vectorToString(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

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
            "ORDER BY dc.embedding_vector <-> :embeddingVector::vector LIMIT :limit", 
            nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("embeddingVector") String embeddingVector, 
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
            "WHERE dc.embedding_vector <-> :embeddingVector::vector < :threshold " +
            "ORDER BY dc.embedding_vector <-> :embeddingVector::vector LIMIT :limit", 
            nativeQuery = true)
    List<DocumentChunk> findSimilarChunksWithThreshold(
            @Param("embeddingVector") String embeddingVector, 
            @Param("threshold") float threshold,
            @Param("limit") int limit);
}