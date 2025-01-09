package com.example.demo.services;

import com.example.demo.entities.Document;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final RestHighLevelClient restHighLevelClient;

    public SearchService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * General search with scoring and explanations.
     */
    public List<Document> searchDocuments(String query, String field, String fileType, String sortField, String sortOrder, Integer page, Integer size) throws IOException {
        if (field == null) field = "content";
        if (page == null) page = 0;
        if (size == null) size = 10;

        // Create the BoolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // Add the "must" clause for the query
        boolQueryBuilder.must(QueryBuilders.matchQuery(field, query));

        // Add the "filter" clause for fileType if provided
        if (fileType != null && !fileType.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("fileType", fileType));
        }

        // Create the SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQueryBuilder)
                .from(page * size)
                .size(size)
                .explain(true); // Enable explanations

        // Add sorting if provided
        if (sortField != null && !sortField.isEmpty()) {
            SortOrder order = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? SortOrder.DESC : SortOrder.ASC;
            searchSourceBuilder.sort(sortField, order);
        }

        // Build the SearchRequest
        SearchRequest searchRequest = new SearchRequest("documents").source(searchSourceBuilder);

        // Execute the search request
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return mapSearchResponseWithExplanation(searchResponse);
    }

    /**
     * Wildcard Search with scoring and explanations.
     */
    public List<Document> wildcardSearch(String pattern, String field) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.wildcardQuery(field, pattern))
                .explain(true); // Enable explanations

        SearchRequest searchRequest = new SearchRequest("documents").source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return mapSearchResponseWithExplanation(searchResponse);
    }

    /**
     * Exact Phrase Search with scoring and explanations.
     */
    public List<Document> exactPhraseSearch(String phrase, String field) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchPhraseQuery(field, phrase))
                .explain(true); // Enable explanations

        SearchRequest searchRequest = new SearchRequest("documents").source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return mapSearchResponseWithExplanation(searchResponse);
    }

    /**
     * Proximity Search with scoring and explanations.
     */
    public List<Document> proximitySearch(String words, String field, int distance) throws IOException {
        MatchPhraseQueryBuilder matchPhraseQuery = QueryBuilders.matchPhraseQuery(field, words).slop(distance);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(matchPhraseQuery)
                .explain(true); // Enable explanations

        SearchRequest searchRequest = new SearchRequest("documents").source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return mapSearchResponseWithExplanation(searchResponse);
    }

    /**
     * Range Search with scoring and explanations.
     */
    public List<Document> rangeSearch(String field, String gte, String lte) throws IOException {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field).gte(gte).lte(lte);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(rangeQueryBuilder)
                .explain(true); // Enable explanations

        SearchRequest searchRequest = new SearchRequest("documents").source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return mapSearchResponseWithExplanation(searchResponse);
    }

    /**
     * Helper method to map search response to Document list with explanations.
     */
   /* private List<Document> mapSearchResponseWithExplanation(SearchResponse searchResponse) {
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
    }*/

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

                    // Add explanation and Lucene score
                    if (hit.getExplanation() != null) {
                        System.out.println("Explanation for document " + hit.getId() + ": " + hit.getExplanation());
                    }
                    document.setLuceneScore(hit.getScore()); // Set the score

                    // Remove fields that match the criteria
                    if ("unknown".equals(document.getAuthor())) {
                        document.setAuthor(null); // Set to null to exclude from the output
                    }

                    return document;
                })
                .map(doc -> {
                    // Remove fields with null or default values
                    doc.setFilePath(null); // Exclude filePath
                    if (doc.getFileSize() == 0) {
                        doc.setFileSize(-1); // Optionally set an invalid value for ignored fields
                    }
                    return doc;
                })
                .collect(Collectors.toList());
    }

}
