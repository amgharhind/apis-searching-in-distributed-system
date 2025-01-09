package com.example.demo.services;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BM25RankingService {
    private final RestHighLevelClient elasticsearchClient;

    public BM25RankingService(RestHighLevelClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<Map<String, Object>> rankWithBM25(String query, String field, String index, int size) throws IOException {
        // Build Elasticsearch query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery(field, query))
                .size(size);

        // Create search request for the specified index
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        // Execute search request
        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        // Format results to match the required structure
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(hit -> Map.of(
                        "id", hit.getId(),
                        "content", hit.getSourceAsMap().get("content"),
                        "bm25_score", hit.getScore()
                ))
                .collect(Collectors.toList());
    }
}
