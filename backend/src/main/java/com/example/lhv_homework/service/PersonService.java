package com.example.lhv_homework.service;

import com.example.lhv_homework.model.Person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<Person> getAllNames() {
        List<Person> names = new ArrayList<>();
        for (String key : redisTemplate.keys("*")) {
            String name = redisTemplate.opsForValue().get(key);
            Long id = Long.valueOf(key);
            names.add(new Person(id, name));
        }
        return names;
    }

    public boolean isValidName(String name) {
        return name != null &&
           !name.trim().isEmpty() &&
           name.matches("^[A-Za-z\\s'-]{2,50}$"); // letters, space, dash, apostrophe, 2–50 chars
    } 

    public boolean isValidId(Long id) {
        return id != null && id > 0;
    }
}
