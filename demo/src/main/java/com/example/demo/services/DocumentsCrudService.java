package com.example.demo.services;

import com.example.demo.entities.Document;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentsCrudService {

    private final RestHighLevelClient elasticsearchClient;
    private final RedisCacheService redisCacheService;

    public DocumentsCrudService(RestHighLevelClient elasticsearchClient, RedisCacheService redisCacheService) {
        this.elasticsearchClient = elasticsearchClient;
        this.redisCacheService = redisCacheService;
    }

    /**
     * Get all documents
     */
    /**
     * Get all documents with explanations.
     */
   /* public List<Document> getAllDocuments() throws IOException {
        SearchRequest searchRequest = new SearchRequest("documents");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .explain(true); // Enable explanations
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits())
                .map(this::mapHitToDocument) // Map explanations to documents
                .collect(Collectors.toList());
    }*/

    /**
     * Get a document by ID
     */
    /*public Optional<Document> getDocumentById(String id) throws IOException {
        // Use SearchRequest for explanation
        SearchRequest searchRequest = new SearchRequest("documents");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.termQuery("_id", id))
                .explain(true); // Enable explanation
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        if (searchResponse.getHits().getTotalHits().value > 0) {
            SearchHit hit = searchResponse.getHits().getHits()[0];
            return Optional.of(mapHitToDocument(hit)); // Map explanation to the document
        }
        return Optional.empty();
    }*/
    /**
     * Update specific fields of a document
     */
    public boolean updateDocumentFields(String id, Map<String, Object> fieldsToUpdate) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("documents", id).doc(fieldsToUpdate);
        UpdateResponse updateResponse = elasticsearchClient.update(updateRequest, RequestOptions.DEFAULT);

        return "UPDATED".equalsIgnoreCase(updateResponse.getResult().name());
    }

    /**
     * Delete a document by ID
     */
    public boolean deleteDocument(String id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("documents", id);
        DeleteResponse deleteResponse = elasticsearchClient.delete(deleteRequest, RequestOptions.DEFAULT);

        return "DELETED".equalsIgnoreCase(deleteResponse.getResult().name());
    }

    /**
     * Delete documents by a specific field and value
     */
    public int deleteDocumentsByCriteria(String field, String value) throws IOException {
        TermQueryBuilder termQuery = QueryBuilders.termQuery(field, value);

        SearchRequest searchRequest = new SearchRequest("documents");
        searchRequest.source(new SearchSourceBuilder().query(termQuery));

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        List<String> idsToDelete = Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getId)
                .collect(Collectors.toList());

        for (String id : idsToDelete) {
            deleteDocument(id);
        }

        return idsToDelete.size();
    }

    /**
     * Get documents with inclusion/exclusion filters
     */
    public List<Document> getDocumentsWithFilters(String includeField, String includeValue, String excludeField, String excludeValue) throws IOException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (includeField != null && includeValue != null) {
            boolQuery.must(QueryBuilders.termQuery(includeField, includeValue));
        }
        if (excludeField != null && excludeValue != null) {
            boolQuery.mustNot(QueryBuilders.termQuery(excludeField, excludeValue));
        }

        SearchRequest searchRequest = new SearchRequest("documents");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .explain(true); // Enable explanation
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits())
                .map(this::mapHitToDocument)
                .collect(Collectors.toList());
    }
    /**
     * Update document fields with cache
     */
    public boolean updateDocumentFieldsWithCache(String id, Map<String, Object> fieldsToUpdate) throws IOException {
        boolean result = updateDocumentFields(id, fieldsToUpdate);
        if (result) {
            redisCacheService.clearAllCache();
        }
        return result;
    }

    /**
     * Delete document with cache
     */
    public boolean deleteDocumentWithCache(String id) throws IOException {
        boolean result = deleteDocument(id);
        if (result) {
            redisCacheService.clearAllCache();
        }
        return result;
    }

    /**
     * Map Elasticsearch source map to Document entity
     */
    private Document mapSourceToDocument(Map<String, Object> sourceMap) {
        Document document = new Document();
        document.setId((String) sourceMap.get("id"));
        document.setFilename((String) sourceMap.get("filename"));
        document.setContent((String) sourceMap.get("content"));
        document.setFileType((String) sourceMap.get("fileType"));
        document.setFilePath((String) sourceMap.get("filePath"));
        document.setFileSize(((Number) sourceMap.getOrDefault("fileSize", 0)).longValue());
        document.setCreatedDate((String) sourceMap.get("createdDate"));
        document.setAuthor((String) sourceMap.get("author"));
        document.setLuceneScore(((Number) sourceMap.getOrDefault("luceneScore", 0.0)).doubleValue());
        document.setFinalScore(((Number) sourceMap.getOrDefault("finalScore", 0.0)).doubleValue());
        return document;
    }

    /**
     * Map SearchHit to Document entity
     */
    private Document mapHitToDocument(SearchHit hit) {
        Document document = mapSourceToDocument(hit.getSourceAsMap());
        document.setId(hit.getId()); // Explicitly set the Elasticsearch ID
        document.setLuceneScore(hit.getScore()); // Set Lucene score

        // Add explanation
        if (hit.getExplanation() != null) {
            document.setExplanation(hit.getExplanation().toString()); // Set explanation if available
        } else {
            document.setExplanation("No explanation available for this document."); // Default if none
        }

        return document;
    }
    public List<Map<String, Object>> getAllDocuments() throws IOException {
        SearchRequest searchRequest = new SearchRequest("documents");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .fetchSource(new String[]{"content"}, null); // Fetch only 'content'
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits())
                .map(hit -> Map.of(
                        "id", hit.getId(),
                        "content", hit.getSourceAsMap().get("content")
                ))
                .collect(Collectors.toList());
    }
    public Optional<Map<String, Object>> getDocumentById(String id) throws IOException {
        SearchRequest searchRequest = new SearchRequest("documents");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.termQuery("_id", id))
                .fetchSource(new String[]{"content"}, null); // Fetch only 'content'
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        if (searchResponse.getHits().getTotalHits().value > 0) {
            SearchHit hit = searchResponse.getHits().getHits()[0];
            return Optional.of(Map.of(
                    "id", hit.getId(),
                    "content", hit.getSourceAsMap().get("content")
            ));
        }
        return Optional.empty();
    }

}
