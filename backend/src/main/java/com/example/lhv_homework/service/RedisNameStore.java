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
    
    private final HashOperations<String, String, String> hashOps;

    private final AtomicLong idGenerator = new AtomicLong();

    @PostConstruct
    public void initializeIdGenerator() {
        Set<String> keys = redisTemplate.keys("sanctioned:*");
        long maxId = keys.stream()
                          .map(key -> key.replace("sanctioned:", ""))
                          .mapToLong(Long::parseLong)
                          .max()
                          .orElse(0L);
        idGenerator.set(maxId + 1);
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public RedisNameStore(RedisTemplate<String, String> redisTemplate) {
        this.hashOps = redisTemplate.opsForHash();
    }

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

    public Long saveSanctionedName(String rawName, String preprocessedName) {
        Long id = idGenerator.incrementAndGet();
        String key = "sanctioned:" + id.toString();
        hashOps.put(key, "raw_name", rawName);
        hashOps.put(key, "preprocessed_name", preprocessedName);
        return id;
    }

    public void updateSanctionedName(String id, String newRawName, String newPreprocessedName) {
        String key = "sanctioned:" + id;
        hashOps.put(key, "raw_name", newRawName);
        hashOps.put(key, "preprocessed_name", newPreprocessedName);
    }

    public String getPreprocessedName(String id) {
        String key = "sanctioned:" + id;
        return hashOps.get(key, "preprocessed_name");
    }

    public String getRawName(String id) {
        String key = "sanctioned:" + id;
        return hashOps.get(key, "raw_name");
    }

    public Map<String, String> getSanctionedEntry(String id) {
        String key = "sanctioned:" + id;
        return hashOps.entries(key);
    }

    public boolean deleteSanctionedEntry(String id) {
        String key = "sanctioned:" + id;
        Boolean result = hashOps.getOperations().delete(key);
        return Boolean.TRUE.equals(result);
    }
}
