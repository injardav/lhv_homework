package com.example.lhv_homework.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.lhv_homework.model.Person;

@Service
public class RedisNameStore {
    
    private final HashOperations<String, String, String> hashOps;

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

    public void saveSanctionedName(String id, String rawName, String preprocessedName) {
        String key = "sanctioned:" + id;
        hashOps.put(key, "raw_name", rawName);
        hashOps.put(key, "preprocessed_name", preprocessedName);
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
