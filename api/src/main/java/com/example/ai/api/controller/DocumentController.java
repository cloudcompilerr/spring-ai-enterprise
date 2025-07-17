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
import java.util.stream.Collectors;

/**
 * REST controller for document operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Endpoint to get all documents.
     *
     * @return List of all documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        try {
            List<Document> documents = documentService.getAllDocuments();
            List<DocumentResponse> responses = documents.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error retrieving all documents", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve documents", e);
        }
    }

    /**
     * Endpoint to create a new document.
     *
     * @param request The document request
     * @return The created document
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentRequest request) {
        try {
            Document document = documentService.createDocument(
                    request.getTitle(),
                    request.getContent(),
                    request.getSourceUrl(),
                    request.getDocumentType()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(document));
        } catch (Exception e) {
            log.error("Error creating document", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create document", e);
        }
    }

    /**
     * Endpoint to get a document by ID.
     *
     * @param id The document ID
     * @return The document if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        try {
            Optional<Document> document = documentService.getDocumentById(id);
            return document
                    .map(doc -> ResponseEntity.ok(mapToResponse(doc)))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with ID: " + id));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving document with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve document", e);
        }
    }

    /**
     * Endpoint to search for documents by title.
     *
     * @param title The title to search for
     * @return List of matching documents
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponse>> searchDocuments(@RequestParam String title) {
        try {
            List<Document> documents = documentService.searchDocumentsByTitle(title);
            List<DocumentResponse> responses = documents.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error searching documents by title: {}", title, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search documents", e);
        }
    }

    /**
     * Endpoint to delete a document.
     *
     * @param id The document ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            // Check if document exists
            if (!documentService.documentExists(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with ID: " + id);
            }
            
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting document with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete document", e);
        }
    }

    /**
     * Maps a document entity to a response DTO.
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