package com.example.demo.services;

import com.example.demo.entities.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.SearchHit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClusterService {

    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClusterService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * Get cluster health
     */
    public JsonNode getClusterHealth() throws IOException {
        ClusterHealthRequest request = new ClusterHealthRequest();
        ClusterHealthResponse response = restHighLevelClient.cluster().health(request, RequestOptions.DEFAULT);

        // Convert response to JSON
        String jsonResponse = objectMapper.writeValueAsString(response);
        return objectMapper.readTree(jsonResponse);
    }

    /**
     * Get all nodes info
     */
    public JsonNode getAllNodesInfo() throws IOException {
        // Use low-level client to fetch nodes info
        Request request = new Request("GET", "/_nodes");
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);

        // Convert response to JSON
        return objectMapper.readTree(response.getEntity().getContent());
    }
    public List<Document> getDocumentsInNode(String nodeId) throws IOException {
        Request request = new Request("GET", "/_cat/shards?format=json");
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);

        JsonNode shards = objectMapper.readTree(response.getEntity().getContent());

        List<String> indicesOnNode = new ArrayList<>();
        shards.forEach(shard -> {
            if (shard.get("node").asText().equals(nodeId)) {
                indicesOnNode.add(shard.get("index").asText());
            }
        });

        List<Document> documents = new ArrayList<>();
        for (String index : indicesOnNode) {
            documents.addAll(getDocumentsFromIndex(index));
        }
        return documents;
    }
    public JsonNode getNodesStats() throws IOException {
        Request request = new Request("GET", "/_nodes/stats");
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);

        return objectMapper.readTree(response.getEntity().getContent());
    }
    public boolean isNodeRunning(String nodeId) throws IOException {
        Request request = new Request("GET", "/_nodes/" + nodeId);
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);

        JsonNode jsonNode = objectMapper.readTree(response.getEntity().getContent());
        return jsonNode.has("nodes") && jsonNode.get("nodes").has(nodeId);
    }

    public List<Document> getDocumentsFromIndex(String index) throws IOException {
        // Create a search request for the specified index
        SearchRequest searchRequest = new SearchRequest(index);

        // Define the query: Match all documents
        searchRequest.source(new SearchSourceBuilder()
                .query(new MatchAllQueryBuilder())
                .size(100) // Limit to 100 documents
        );

        // Execute the search request
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // Map SearchHits to Document objects
        List<Document> documents = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Document document = objectMapper.convertValue(hit.getSourceAsMap(), Document.class);
            documents.add(document);
        }

        return documents;
    }
    public JsonNode getNodeContent(String nodeId) throws IOException {
        // Create a low-level request for the node's stats
        Request request = new Request("GET", "/_nodes/" + nodeId + "/stats");
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);

        // Convert the response to a JSON node
        String jsonResponse = org.apache.commons.io.IOUtils.toString(response.getEntity().getContent(), java.nio.charset.StandardCharsets.UTF_8);
        return objectMapper.readTree(jsonResponse);
    }
}
