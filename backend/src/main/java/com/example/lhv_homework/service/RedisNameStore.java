package com.example.lhv_homework.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.lhv_homework.model.Person;
import jakarta.annotation.PostConstruct;

@Service
public class RedisNameStore {
    
    @Autowired private RedisTemplate<String, String> redisTemplate;

    private final HashOperations<String, String, String> hashOps;
    private final AtomicLong idGenerator = new AtomicLong();

    @PostConstruct
    /**
     * Initializes the ID generator by finding the maximum ID currently stored in Redis.
     * This ensures that new IDs are generated sequentially without conflicts.
     */
    public void initializeIdGenerator() {
        Set<String> keys = redisTemplate.keys("sanctioned:*");
        long maxId = keys.stream()
                          .map(key -> key.replace("sanctioned:", ""))
                          .mapToLong(Long::parseLong)
                          .max()
                          .orElse(0L);
        idGenerator.set(maxId + 1);
    }

    /**
     * Constructor for RedisNameStore.
     * Initializes the HashOperations for interacting with Redis.
     *
     * @param redisTemplate the RedisTemplate used for Redis operations.
     */
    public RedisNameStore(RedisTemplate<String, String> redisTemplate) {
        this.hashOps = redisTemplate.opsForHash();
    }

    /**
     * Retrieves all sanctioned names from Redis.
     * Each name is represented as a Person object containing ID, raw name, and preprocessed name.
     *
     * @return a list of Person objects representing all sanctioned names.
     */
    public List<Person> getAllNames() {
        Set<String> keys = redisTemplate.keys("sanctioned:*");
        if (keys == null || keys.isEmpty()) return List.of();
        return keys.stream()
                   .map(key -> {
                       String id = key.replace("sanctioned:", "");
                       String rawName = hashOps.get(key, "raw_name");
                       String preprocessedName = hashOps.get(key, "preprocessed_name");
                       return new Person(Long.valueOf(id), rawName, preprocessedName);
                   })
                   .toList();
    }

    /**
     * Saves a new sanctioned name in Redis.
     * Generates a unique ID for the name and stores both the raw and preprocessed versions.
     *
     * @param rawName the original name as provided by the user.
     * @param preprocessedName the preprocessed version of the name for matching purposes.
     * @return the generated ID for the newly stored sanctioned name.
     */
    public Long saveSanctionedName(String rawName, String preprocessedName) {
        Long id = idGenerator.incrementAndGet();
        String key = "sanctioned:" + id.toString();
        hashOps.put(key, "raw_name", rawName);
        hashOps.put(key, "preprocessed_name", preprocessedName);
        return id;
    }

    /**
     * Updates an existing sanctioned name in Redis.
     *
     * @param id the ID of the sanctioned name to update.
     * @param newRawName the new raw name to store.
     * @param newPreprocessedName the new preprocessed name to store.
     */
    public void updateSanctionedName(String id, String newRawName, String newPreprocessedName) {
        String key = "sanctioned:" + id;
        hashOps.put(key, "raw_name", newRawName);
        hashOps.put(key, "preprocessed_name", newPreprocessedName);
    }

    /**
     * Retrieves the preprocessed name for a given ID.
     *
     * @param id the ID of the sanctioned name.
     * @return the preprocessed name, or null if not found.
     */
    public String getPreprocessedName(String id) {
        String key = "sanctioned:" + id;
        return hashOps.get(key, "preprocessed_name");
    }

    /**
     * Retrieves the raw name for a given ID.
     *
     * @param id the ID of the sanctioned name.
     * @return the raw name, or null if not found.
     */
    public String getRawName(String id) {
        String key = "sanctioned:" + id;
        return hashOps.get(key, "raw_name");
    }

    /**
     * Retrieves a sanctioned entry by its ID.
     * The entry is returned as a map containing both raw and preprocessed names.
     *
     * @param id the ID of the sanctioned entry.
     * @return a map with keys "raw_name" and "preprocessed_name", or an empty map if not found.
     */
    public Map<String, String> getSanctionedEntry(String id) {
        String key = "sanctioned:" + id;
        return hashOps.entries(key);
    }

    /**
     * Deletes a sanctioned entry by its ID.
     *
     * @param id the ID of the sanctioned entry to delete.
     * @return true if the entry was successfully deleted, false otherwise.
     */
    public boolean deleteSanctionedEntry(String id) {
        String key = "sanctioned:" + id;
        Boolean result = hashOps.getOperations().delete(key);
        return Boolean.TRUE.equals(result);
    }
}
