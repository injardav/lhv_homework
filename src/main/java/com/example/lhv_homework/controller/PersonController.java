package com.example.lhv_homework.controller;

import com.example.lhv_homework.model.Person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.atomic.AtomicLong;


@RestController
@RequestMapping("/api/v1/names")
public class PersonController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final AtomicLong idGenerator = new AtomicLong();

    @GetMapping
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        String name = redisTemplate.opsForValue().get(id.toString());

        Person person = new Person(id, name);
        return ResponseEntity.status(HttpStatus.OK).body(person);
    }

    @PostMapping
    public ResponseEntity<Person> createPerson(@RequestBody String name) {
        Long id = idGenerator.incrementAndGet();
        redisTemplate.opsForValue().set(id.toString(), name);

        Person person = new Person(id, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @DeleteMapping("/{id}")
    public boolean deletePersonById(@PathVariable Long id) {
        return redisTemplate.delete(id.toString());
    }
}