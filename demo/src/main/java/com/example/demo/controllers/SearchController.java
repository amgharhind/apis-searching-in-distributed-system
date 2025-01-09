package com.example.demo.controllers;

import com.example.demo.entities.Document;
import com.example.demo.services.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * General Search Endpoint
     */
    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam("query") String query, // Required
            @RequestParam(value = "field", defaultValue = "content", required = false) String field, // Optional
            @RequestParam(value = "fileType", required = false) String fileType, // Optional
            @RequestParam(value = "sortField", required = false) String sortField, // Optional
            @RequestParam(value = "sortOrder", defaultValue = "asc", required = false) String sortOrder, // Optional
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page, // Optional
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size // Optional
    ) {
        try {
            // Call search service with parameters
            List<Document> results = searchService.searchDocuments(query, field, fileType, sortField, sortOrder, page, size);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error performing search: " + e.getMessage());
        }
    }

    /**
     * Wildcard Search
     */
    @GetMapping("/wildcard")
    public ResponseEntity<?> wildcardSearch(@RequestParam("pattern") String pattern, @RequestParam("field") String field) {
        try {
            List<Document> results = searchService.wildcardSearch(pattern, field);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error performing wildcard search: " + e.getMessage());
        }
    }

    /**
     * Exact Phrase Search
     */
    @GetMapping("/exact-phrase")
    public ResponseEntity<?> exactPhraseSearch(@RequestParam("phrase") String phrase, @RequestParam("field") String field) {
        try {
            List<Document> results = searchService.exactPhraseSearch(phrase, field);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error performing exact phrase search: " + e.getMessage());
        }
    }

    /**
     * Proximity Search
     */
    @GetMapping("/proximity")
    public ResponseEntity<?> proximitySearch(
            @RequestParam("words") String words,
            @RequestParam("field") String field,
            @RequestParam("distance") int distance) {
        try {
            List<Document> results = searchService.proximitySearch(words, field, distance);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error performing proximity search: " + e.getMessage());
        }
    }

    /**
     * Range Search
     */
    @GetMapping("/range")
    public ResponseEntity<?> rangeSearch(@RequestParam("field") String field, @RequestParam("gte") String gte, @RequestParam("lte") String lte) {
        try {
            List<Document> results = searchService.rangeSearch(field, gte, lte);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error performing range search: " + e.getMessage());
        }
    }
}
