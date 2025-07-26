package com.cloudcompilerr.ai.rag.repository;

import com.cloudcompilerr.ai.rag.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Document entities.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find documents by title.
     *
     * @param title The title to search for
     * @return List of matching documents
     */
    List<Document> findByTitleContainingIgnoreCase(String title);

    /**
     * Find documents by document type.
     *
     * @param documentType The document type to search for
     * @return List of matching documents
     */
    List<Document> findByDocumentType(String documentType);

    /**
     * Find a document by its source URL.
     *
     * @param sourceUrl The source URL to search for
     * @return Optional containing the document if found
     */
    Optional<Document> findBySourceUrl(String sourceUrl);
}