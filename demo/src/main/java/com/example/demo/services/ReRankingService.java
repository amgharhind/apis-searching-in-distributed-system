package com.example.demo.services;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ReRankingService {
    private final RestTemplate restTemplate = new RestTemplate();
    public List<Map<String, Object>> reRankDocuments(String query, String index) {
        try {
            String url = "http://localhost:5000/re-rank";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("index", index);
            requestBody.put("size", 10); // Top 10 results
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return (List<Map<String, Object>>) response.getBody().get("ranked_results");
            } else {
                throw new RuntimeException("Re-ranking failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling re-ranking service: " + e.getMessage());
        }
    }
}