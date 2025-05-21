package com.example.lhv_homework.controller;

import com.example.lhv_homework.model.Person;
import com.example.lhv_homework.service.PersonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/names")
public class PersonController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PersonService personService;

    private final AtomicLong idGenerator = new AtomicLong();

    @GetMapping
    public ResponseEntity<List<Person>> getAllNames() {
        List<Person> names = personService.getAllNames();
        return ResponseEntity.status(HttpStatus.OK).body(names);
    }

    @GetMapping("/{id}")
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
    public ResponseEntity<Void> deletePersonById(@PathVariable Long id) {
        // Check if ID is valid
        if (!personService.isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean deleted = redisTemplate.delete(id.toString());

        return deleted
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}