package com.example.demo.controllers;
import com.example.demo.entities.Document;
import com.example.demo.services.DocumentsCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/documents")
public class DocumentsCrudController {
private final DocumentsCrudService documentService;

    public DocumentsCrudController(DocumentsCrudService documentService) {
        this.documentService = documentService;
    }


    @GetMapping
    public ResponseEntity<?> getAllDocuments() {
        try {
            List<Map<String, Object>> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    /**
     * Get a document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable("id") String id) {
        try {
            Optional<Map<String, Object>> document = documentService.getDocumentById(id);
            if (document.isPresent()) {
                return ResponseEntity.ok(document.get());
            } else {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Document not found"));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }




    /**
     * Delete a single document by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable("id") String id) {
        try {
            boolean deleted = documentService.deleteDocument(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Document not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting document", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteDocumentsByCriteria(@RequestParam("field") String field, @RequestParam("value") String value) {
        try {
            int deletedCount = documentService.deleteDocumentsByCriteria(field, value);
            return ResponseEntity.ok(Map.of("message", deletedCount + " documents deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting documents", "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateDocumentFields(@PathVariable("id") String id, @RequestBody Map<String, Object> fieldsToUpdate) {
        try {
            boolean updated = documentService.updateDocumentFields(id, fieldsToUpdate);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Document updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Document not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating document", "message", e.getMessage()));
        }
    }
    /**
     * Get documents with inclusion/exclusion filters
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getDocumentsWithFilters(
            @RequestParam(value = "includeField", required = false) String includeField,
            @RequestParam(value = "includeValue", required = false) String includeValue,
            @RequestParam(value = "excludeField", required = false) String excludeField,
            @RequestParam(value = "excludeValue", required = false) String excludeValue) {
        try {
            List<Document> documents = documentService.getDocumentsWithFilters(includeField, includeValue, excludeField, excludeValue);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/cache/{id}")
    public ResponseEntity<?> updateDocumentFieldsWithCache(@PathVariable("id") String id, @RequestBody Map<String, Object> fieldsToUpdate) {
        try {
            boolean updated = documentService.updateDocumentFieldsWithCache(id, fieldsToUpdate);
            if (updated) {
                return ResponseEntity.ok("Document updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete a single document by ID
     */
    @DeleteMapping("/cache/{id}")
    public ResponseEntity<?> deleteDocumentWithCache(@PathVariable("id") String id) {
        try {
            boolean deleted = documentService.deleteDocumentWithCache(id);
            if (deleted) {
                return ResponseEntity.ok("Document deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
