package com.example.ai.api.controller;

import com.example.ai.api.dto.DocumentRequest;
import com.example.ai.api.dto.DocumentResponse;
import com.example.ai.rag.model.Document;
import com.example.ai.rag.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * REST controller for document operations.
 * Updated to use Java 21 features.
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Endpoint to get all documents.
     * Uses Java 21 enhanced Stream API and error handling.
     *
     * @return List of all documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        try {
            // Using Java 21 enhanced Stream API with toList() collector
            List<DocumentResponse> responses = documentService.getAllDocuments().stream()
                    .map(this::mapToResponse)
                    .toList();
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error retrieving all documents", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve documents", e);
        }
    }

    /**
     * Endpoint to create a new document.
     * Uses Java 21 pattern matching and records.
     *
     * @param request The document request
     * @return The created document
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentRequest request) {
        try {
            // Using Java 21 record pattern matching
            var document = documentService.createDocument(
                    request.title(),
                    request.content(),
                    request.sourceUrl(),
                    request.documentType()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(document));
        } catch (Exception e) {
            log.error("Error creating document", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create document", e);
        }
    }

    /**
     * Endpoint to get a document by ID.
     * Uses Java 21 enhanced Optional handling and pattern matching.
     *
     * @param id The document ID
     * @return The document if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        try {
            // Using Java 21 enhanced Optional handling with pattern matching
            return documentService.getDocumentById(id)
                    .map(this::mapToResponse)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Document not found with ID: " + id
                    ));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving document with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve document", e);
        }
    }

    /**
     * Endpoint to search for documents by title.
     * Uses Java 21 enhanced Stream API and functional programming.
     *
     * @param title The title to search for
     * @return List of matching documents
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponse>> searchDocuments(@RequestParam String title) {
        // Using Java 21 record for search parameters
        record SearchParams(String title) {}
        var params = new SearchParams(title);
        
        try {
            // Using Java 21 enhanced functional programming
            Function<String, List<DocumentResponse>> searchFunction = searchTitle -> 
                documentService.searchDocumentsByTitle(searchTitle).stream()
                    .map(this::mapToResponse)
                    .toList();
            
            var responses = searchFunction.apply(params.title());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error searching documents by title: {}", params.title(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search documents", e);
        }
    }

    /**
     * Endpoint to delete a document.
     * Uses Java 21 enhanced error handling and pattern matching.
     *
     * @param id The document ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            // Using Java 21 pattern matching in switch expression
            return switch (documentService.documentExists(id)) {
                case true -> {
                    documentService.deleteDocument(id);
                    yield ResponseEntity.noContent().build();
                }
                case false -> throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Document not found with ID: " + id
                );
            };
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting document with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete document", e);
        }
    }

    /**
     * Maps a document entity to a response DTO.
     * Uses Java 21 record constructors.
     *
     * @param document The document entity
     * @return The document response DTO
     */
    private DocumentResponse mapToResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getSourceUrl(),
                document.getDocumentType(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}