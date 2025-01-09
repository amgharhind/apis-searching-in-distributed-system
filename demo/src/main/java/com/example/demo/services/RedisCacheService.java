package com.example.demo.services;
import com.example.demo.entities.Document;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper; // For JSON (de)serialization
    private final SearchService searchService;
    public RedisCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper,SearchService searchService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.searchService = searchService;
    }

    /**
     * Save data to cache
     */
    // Serialize and save to cache
    /**public void saveToCache(String key, Object value, long timeoutSeconds) throws JsonProcessingException {
        String jsonValue = objectMapper.writeValueAsString(value);
        redisTemplate.opsForValue().set(key, jsonValue, timeoutSeconds, TimeUnit.SECONDS);
    }

    // Deserialize and fetch from cache
    public <T> T getFromCache(String key, Class<T> valueType) throws JsonProcessingException {
        String jsonValue = redisTemplate.opsForValue().get(key);
        if (jsonValue == null) {
            return null;
        }
        return objectMapper.readValue(jsonValue, valueType);
    }

    // Delete a single key
    public void deleteFromCache(String key) {
        redisTemplate.delete(key);
    }
     **/
    // Get all keys
    public Set<String> getAllCachedKeys() {
        return redisTemplate.keys("*"); // Match all keys
    }

    // Clear all cache
    public void clearAllCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }
    /**
     * Cache a query result.
     *
     * @param key           the cache key.
     * @param value         the value to cache.
     * @param timeoutSeconds the cache expiration time in seconds.
     */
    public void cacheQuery(String key, Object value, long timeoutSeconds) throws JsonProcessingException {
        String jsonValue = objectMapper.writeValueAsString(value);
        redisTemplate.opsForValue().set(key, jsonValue, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Retrieve cached query result.
     *
     * @param key the cache key.
     * @param <T> the expected result type.
     * @return the cached result, or null if not found.
     */
    public <T> T getCachedQuery(String key, Class<T> valueType) throws JsonProcessingException {
        String jsonValue = redisTemplate.opsForValue().get(key);
        if (jsonValue == null) {
            return null;
        }
        return objectMapper.readValue(jsonValue, valueType);
    }


    /**
     * Clear a specific cached query.
     *
     * @param key the cache key to clear.
     */
    public void clearCache(String key) {
        redisTemplate.delete(key);
    }

    public void clearCache(String query, String field, int page, int size) {
        String cacheKey = String.format("search::%s::%s::%d::%d", query, field, page, size);
        clearCache(cacheKey);
    }
    public boolean validateCachedQuery(String query, String field, String fileType, String sortField, String sortOrder, Integer page, Integer size) throws IOException {
        String cacheKey = String.format("search::%s::%s::%s::%s::%d::%d", query, field, fileType, sortField, page, size);

        // Retrieve from cache
        List<Document> cachedDocuments = getCachedQuery(cacheKey, List.class);
        System.out.println("Cached Results: " + cachedDocuments);

        // Perform the same query without cache
        List<Document> freshDocuments = searchService.searchDocuments(query, field, fileType, sortField, sortOrder, page, size);
        System.out.println("Elasticsearch Results: " + freshDocuments);

        // Compare the two lists
        if (cachedDocuments == null || cachedDocuments.isEmpty()) {
            System.err.println("Cached results are null or empty.");
            return false;
        }

        boolean isEqual = cachedDocuments.equals(freshDocuments);
        if (!isEqual) {
            System.err.println("Mismatch Detected: Cached Results do not match Elasticsearch Results.");
        }
        return isEqual;
    }


}
