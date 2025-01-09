package com.example.demo.services;
import com.example.demo.entities.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InteractionsReRankingService {

    private final SearchService searchService;
    private final RedisTemplate<String, String> redisTemplate;

    public InteractionsReRankingService(SearchService searchService, RedisTemplate<String, String> redisTemplate) {
        this.searchService = searchService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Perform an initial search and apply re-ranking using interactions
     */
    public List<Document> searchAndRank(
            String query, String field, String fileType, String sortField,
            String sortOrder, Integer page, Integer size
    ) throws Exception {
        // Perform the initial search
        List<Document> results = searchService.searchDocuments(query, field, fileType, sortField, sortOrder, page, size);

        // Re-rank results using interaction scores from Redis
        results.forEach(doc -> {
            String interactionKey = "doc::interaction::" + doc.getId();
            String interactionValue = redisTemplate.opsForValue().get(interactionKey);
            int interactionScore = (interactionValue != null) ? Integer.parseInt(interactionValue) : 0;

            // Update the document's score (weighted sum of Lucene score and interaction score)
            double reRankedScore = 0.8 * doc.getLuceneScore() + 0.2 * interactionScore;
            doc.setFinalScore(reRankedScore);
        });

        // Sort the results by the final score
        return results.stream()
                .sorted((doc1, doc2) -> Double.compare(doc2.getFinalScore(), doc1.getFinalScore()))
                .collect(Collectors.toList());
    }

    /**
     * Increment interaction score for a document.
     */
    public void incrementInteraction(String documentId) {
        String interactionKey = "doc::interaction::" + documentId;
        redisTemplate.opsForValue().increment(interactionKey, 1);
    }
}
