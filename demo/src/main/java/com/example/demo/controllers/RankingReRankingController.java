package com.example.demo.controllers;
import com.example.demo.entities.Document;
import com.example.demo.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/re-ranking")
public class RankingReRankingController {

    private final BM25RankingService bm25RankingService;

    private final InteractionsReRankingService interactionsReRankingService;
    private final ReRankingService reRankingService;
    public RankingReRankingController(InteractionsReRankingService interactionsReRankingService,
                                      BM25RankingService bm25RankingService,
                                       ReRankingService reRankingService) {
        this.interactionsReRankingService= interactionsReRankingService;
        this.bm25RankingService = bm25RankingService;

        this.reRankingService = reRankingService;
    }

    /*
      Search and rank using re-ranking. (using interaction scores from Redis)

     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAndRank(
            @RequestParam("query") String query,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        try {
            List<Document> rankedResults = interactionsReRankingService.searchAndRank(query, field, fileType, sortField, sortOrder, page, size);
            return ResponseEntity.ok(rankedResults);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error performing re-ranked search: " + e.getMessage());
        }
    }

    /*
      Increment interaction score for a document.
     */
    @PostMapping("/interaction")
    public ResponseEntity<?> logInteraction(@RequestParam("documentId") String documentId) {
        try {
            interactionsReRankingService.incrementInteraction(documentId);
            return ResponseEntity.ok("Interaction logged for document: " + documentId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error logging interaction: " + e.getMessage());
        }
    }
  /*  Re-ranking pipeline :
 step1 : perform the initial ranking using full-text-search = bm25 or vector or hybrid ranking .
 step2 : perform semantic or learning to rank algorithms.
 */
    /*
     * Perform BM25 ranking.
     */
  @GetMapping("/bm25-only")
  public ResponseEntity<?> bm25Only(
          @RequestParam("query") String query,
          @RequestParam(value = "field", defaultValue = "content") String field,
          @RequestParam("index") String index,
          @RequestParam(value = "size", defaultValue = "10") int size) {
      try {
          // Perform BM25 ranking
          List<Map<String, Object>> results = bm25RankingService.rankWithBM25(query, field, index, size);

          // Return formatted response
          return ResponseEntity.ok(Map.of(
                  "query", query,
                  "index", index,
                  "ranked_results", results
          ));
      } catch (IOException e) {
          return ResponseEntity.status(500).body("Error performing BM25 ranking: " + e.getMessage());
      }
  }

    @GetMapping("/pipeline")
    public ResponseEntity<?> executePipeline(
            @RequestParam("query") String query,
            @RequestParam("index") String index) {

        try {
            // Call the re-ranking service to fetch results
            List<Map<String, Object>> reRankedResults = reRankingService.reRankDocuments(query, index);

            // Return the re-ranked results
            return ResponseEntity.ok(Map.of(
                    "query", query,
                    "index", index,
                    "ranked_results", reRankedResults
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error executing pipeline: " + e.getMessage());
        }
    }
}
