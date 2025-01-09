package com.example.demo.controllers;

import com.example.demo.services.RedisCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final RedisCacheService redisCacheService;

    public CacheController( RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }


    /**
     * Retrieve all cached documents
     */
    /**@GetMapping("/documents")
    public ResponseEntity<?> getCachedDocuments() {
        try {
            List<Document> cachedDocuments = searchServiceWithCache.getCachedDocuments();
            return ResponseEntity.ok(cachedDocuments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving cached documents: " + e.getMessage());
        }
    }
**/
    /**
     * View cached keys
     */
    @GetMapping("/keys")
    public ResponseEntity<?> getCachedKeys() {
        try {
            var cachedKeys = redisCacheService.getAllCachedKeys();
            return ResponseEntity.ok(cachedKeys);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving cached keys: " + e.getMessage());
        }
    }

    /**
     * Clear specific cache
     */
    @DeleteMapping
    public ResponseEntity<?> clearCache(
            @RequestParam("query") String query,
            @RequestParam("field") String field,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            redisCacheService.clearCache(query, field, page, size);
            return ResponseEntity.ok("Cache cleared for the specified query.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing cache: " + e.getMessage());
        }
    }

    /**
     * Clear all cache
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAllCache() {
        try {
            redisCacheService.clearAllCache();
            return ResponseEntity.ok(Map.of("status", "success", "message", "All cache cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to clear all cache", "message", e.getMessage()));
        }
    }

}
