package com.example.ai.api.controller;

import com.example.ai.api.dto.DocumentRequest;
import com.example.ai.api.dto.DocumentResponse;
import com.example.ai.rag.model.Document;
import com.example.ai.rag.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for document operations.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Endpoint to create a new document.
     *
     * @param request The document request
     * @return The created document
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentRequest request) {
        Document document = documentService.createDocument(
                request.getTitle(),
                request.getContent(),
                request.getSourceUrl(),
                request.getDocumentType()
        );
        return ResponseEntity.ok(mapToResponse(document));
    }

    /**
     * Endpoint to get a document by ID.
     *
     * @param id The document ID
     * @return The document if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        Optional<Document> document = documentService.getDocumentById(id);
        return document
                .map(doc -> ResponseEntity.ok(mapToResponse(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to search for documents by title.
     *
     * @param title The title to search for
     * @return List of matching documents
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponse>> searchDocuments(@RequestParam String title) {
        List<Document> documents = documentService.searchDocumentsByTitle(title);
        List<DocumentResponse> responses = documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint to delete a document.
     *
     * @param id The document ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
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