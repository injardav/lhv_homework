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

    @Autowired
    private PersonService personService;

    private final AtomicLong idGenerator = new AtomicLong();

    @GetMapping
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        // Check if ID is valid
        if (!personService.isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        
        String name = redisTemplate.opsForValue().get(id.toString());
        
        // Check if the person exists
        if (name == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
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

    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @RequestBody String newName) {
        // Check if ID is valid
        if (!personService.isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }

        // Check if newName is valid
        if (!personService.isValidName(newName)) {
            return ResponseEntity.badRequest().build();
        }

        String existing = redisTemplate.opsForValue().get(id.toString());

        // Check if the person exists
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        redisTemplate.opsForValue().set(id.toString(), newName);

        Person person = new Person(id, newName);
        return ResponseEntity.status(HttpStatus.OK).body(person);
    }

    @DeleteMapping("/{id}")
    public boolean deletePersonById(@PathVariable Long id) {
        // Check if ID is valid
        if (!personService.isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean deleted = redisTemplate.delete(id.toString());

        if (deleted) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}