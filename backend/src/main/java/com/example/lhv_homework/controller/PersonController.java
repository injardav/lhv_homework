package com.example.lhv_homework.controller;

import com.example.lhv_homework.model.Person;
import com.example.lhv_homework.service.PersonService;
import com.example.lhv_homework.service.RedisNameStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/names")
public class PersonController {

    @Autowired
    private RedisNameStore redisNameStore;

    @Autowired
    private PersonService personService;

    @GetMapping
    public ResponseEntity<List<Person>> getAllNames() {
        List<Person> names = redisNameStore.getAllNames();
        return ResponseEntity.ok().body(names);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        // Check if ID is valid
        if (!personService.isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, String> sanctionedEntry = redisNameStore.getSanctionedEntry(id.toString());
        String name = sanctionedEntry.get("raw_name");
        String preprocessedName = sanctionedEntry.get("preprocessed_name");
        
        // Check if the person exists
        if (name == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Person person = new Person(id, name, preprocessedName);
        return ResponseEntity.ok().body(person);
    }

    @PostMapping
    public ResponseEntity<Person> createPerson(@RequestBody String name) {
        String preprocessedName = personService.preprocessName(name);
        Long id = redisNameStore.saveSanctionedName(name, preprocessedName);

        Person person = new Person(id, name, preprocessedName);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @PostMapping("verify")
    public ResponseEntity<Map<String, Object>> verifyPerson(@RequestBody String name) {
        Map<String, Object> verification = personService.verifyName(name);
        return ResponseEntity.ok().body(verification);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @RequestBody String newName) {
        if (
            !personService.isValidId(id) ||
            !personService.isValidName(newName)
        ) {
            return ResponseEntity.badRequest().build();
        }

        String existing = redisNameStore.getRawName(id.toString());

        // Check if the person exists
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        String preprocessedName = personService.preprocessName(newName);
        redisNameStore.updateSanctionedName(id.toString(), newName, preprocessedName);

        Person person = new Person(id, newName, preprocessedName);
        return ResponseEntity.ok().body(person);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonById(@PathVariable Long id) {
        if (!personService.isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }

        boolean deleted = redisNameStore.deleteSanctionedEntry(id.toString());

        return deleted
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}