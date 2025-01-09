package com.example.demo.services;

import com.example.demo.entities.Document;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceWithCache {

    private final RestHighLevelClient restHighLevelClient;
    private final RedisCacheService redisCacheService;

    public SearchServiceWithCache(RestHighLevelClient restHighLevelClient, RedisCacheService redisCacheService) {
        this.restHighLevelClient = restHighLevelClient;
        this.redisCacheService = redisCacheService;
    }

    /**
     * Search documents with cache, scoring, and explanations.
     */
    public List<Document> searchDocumentsWithCache(String query, String field, String fileType, String sortField, String sortOrder, Integer page, Integer size) throws IOException {
        String cacheKey = String.format("search::%s::%s::%s::%s::%d::%d", query, field, fileType, sortField, page, size);

        // Check Redis cache
        List<Document> cachedDocuments = redisCacheService.getCachedQuery(cacheKey, List.class);
        if (cachedDocuments != null) {
            return cachedDocuments; // Return cached results
        }

        // Perform the search query if no cache is found
        List<Document> documents = performSearch(query, field, fileType, sortField, sortOrder, page, size);

        // Cache the results in Redis for 10 minutes
        redisCacheService.cacheQuery(cacheKey, documents, 600);

        return documents;
    }

    /**
     * Perform search operation in Elasticsearch with scoring and explanations.
     */
    public List<Document> performSearch(String query, String field, String fileType, String sortField, String sortOrder, Integer page, Integer size) throws IOException {
        if (field == null) field = "content";
        if (page == null) page = 0;
        if (size == null) size = 10;

        // Create the BoolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // Add the "must" clause for the query
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, query);
        boolQueryBuilder.must(matchQueryBuilder);

        // Add the "filter" clause for fileType if provided
        if (fileType != null && !fileType.isEmpty()) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("fileType", fileType);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // Build the SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQueryBuilder)
                .from(page * size)
                .size(size)
                .explain(true); // Enable explanations

        // Add sorting only if `sortField` is provided
        if (sortField != null && !sortField.isEmpty()) {
            SortOrder order = (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? SortOrder.DESC : SortOrder.ASC;
            searchSourceBuilder.sort(sortField, order);
        }

        // Build the SearchRequest
        SearchRequest searchRequest = new SearchRequest("documents");
        searchRequest.source(searchSourceBuilder);

        // Perform the search request
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // Convert the hits to a List of Documents
        return mapSearchResponseWithExplanation(searchResponse);
    }

    /**
     * Helper method to map search response to Document list with explanations.
     */
    private List<Document> mapSearchResponseWithExplanation(SearchResponse searchResponse) {
        return List.of(searchResponse.getHits().getHits())
                .stream()
                .map(hit -> {
                    Document document = new Document();
                    document.setId(hit.getId());
                    document.setContent(hit.getSourceAsMap().get("content").toString());
                    document.setFilename(hit.getSourceAsMap().get("filename").toString());
                    document.setFileType(hit.getSourceAsMap().get("fileType").toString());
                    document.setCreatedDate(hit.getSourceAsMap().get("createdDate").toString());
                    document.setAuthor(hit.getSourceAsMap().get("author").toString());

                    // Add explanation and Lucene score
                    if (hit.getExplanation() != null) {
                        System.out.println("Explanation for document " + hit.getId() + ": " + hit.getExplanation());
                    }
                    document.setLuceneScore(hit.getScore()); // Set the score

                    return document;
                })
                .collect(Collectors.toList());
    }

    /**
     * Clear cache for a specific query.
     */
    public void clearCache(String query, String field, String fileType, String sortField, Integer page, Integer size) {
        String cacheKey = String.format("search::%s::%s::%s::%s::%d::%d", query, field, fileType, sortField, page, size);
        redisCacheService.clearCache(cacheKey);
    }
}
