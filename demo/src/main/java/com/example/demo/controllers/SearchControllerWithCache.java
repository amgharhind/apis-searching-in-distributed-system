package com.example.demo.controllers;
import com.example.demo.entities.Document;
import com.example.demo.services.RedisCacheService;
import com.example.demo.services.SearchServiceWithCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchControllerWithCache {

    private final SearchServiceWithCache searchServiceWithCache;
    private final RedisCacheService redisCacheService;
    public SearchControllerWithCache(SearchServiceWithCache searchServiceWithCache, RedisCacheService redisCacheService) {
        this.searchServiceWithCache = searchServiceWithCache;
        this.redisCacheService = redisCacheService;
    }

    /**
     * Perform a search with caching enabled.
     */
    @GetMapping("/with-cache")
    public ResponseEntity<?> searchWithCache(
            @RequestParam("query") String query,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            List<Document> documents = searchServiceWithCache.searchDocumentsWithCache(query, field, fileType, sortField, sortOrder, page, size);
            return ResponseEntity.ok(documents);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error performing search with cache: " + e.getMessage());
        }
    }

    /**
     * Clear the cache for a specific query.
     */
    @DeleteMapping("/clear-cache")
    public ResponseEntity<?> clearCache(
            @RequestParam("query") String query,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            searchServiceWithCache.clearCache(query, field, fileType, sortField, page, size);
            return ResponseEntity.ok("Cache cleared for the specified query.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing cache: " + e.getMessage());
        }
    }
    @GetMapping("/validate-cache")
    public ResponseEntity<?> validateCache(
            @RequestParam("query") String query,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            boolean isValid = redisCacheService.validateCachedQuery(query, field, fileType, sortField, sortOrder, page, size);
            if (isValid) {
                return ResponseEntity.ok("Cached results match Elasticsearch results.");
            } else {
                return ResponseEntity.ok("Cached results do NOT match Elasticsearch results.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error validating cached results: " + e.getMessage());
        }
    }
}
