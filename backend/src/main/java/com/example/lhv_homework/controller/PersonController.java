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

    @Autowired private RedisNameStore redisNameStore;
    @Autowired private PersonService personService;

    @GetMapping
    /**
     * Retrieves all sanctioned names currently stored in Redis.
     *
     * @return a ResponseEntity containing a list of Person objects.
     */
    public ResponseEntity<List<Person>> getAllNames() {
        List<Person> names = redisNameStore.getAllNames();
        return ResponseEntity.ok().body(names);
    }

    @GetMapping("/{id}")
    /**
     * Retrieves a specific person entry by ID.
     *
     * @param id the ID of the person to retrieve.
     * @return a ResponseEntity with the Person object if found,
     *         400 Bad Request if the ID is invalid,
     *         or 404 Not Found if no matching entry exists.
     */
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
    /**
     * Creates a new person entry in the sanctioned list.
     *
     * @param name the raw name input to store.
     * @return a ResponseEntity with the created Person object and HTTP 201 status.
     */
    public ResponseEntity<Person> createPerson(@RequestBody String name) {
        String preprocessedName = personService.preprocessName(name);
        Long id = redisNameStore.saveSanctionedName(name, preprocessedName);

        Person person = new Person(id, name, preprocessedName);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @PostMapping("verify")
    /**
     * Verifies whether the input name matches any entry in the sanctioned list
     * using similarity and phonetic matching.
     *
     * @param name the input name to verify.
     * @return a ResponseEntity containing the verification result as a map with match indicators.
     */
    public ResponseEntity<Map<String, Object>> verifyPerson(@RequestBody String name) {
        Map<String, Object> verification = personService.verifyName(name);
        return ResponseEntity.ok().body(verification);
    }

    @PutMapping("/{id}")
    /**
     * Updates the name of a person entry by ID.
     *
     * @param id the ID of the person to update.
     * @param newName the new name to replace the existing entry.
     * @return a ResponseEntity with the updated Person object,
     *         400 Bad Request if the ID or name is invalid,
     *         or 404 Not Found if no entry exists for the given ID.
     */
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
    /**
     * Deletes a person entry from the sanctioned list by ID.
     *
     * @param id the ID of the person to delete.
     * @return a ResponseEntity with HTTP 200 OK if deleted,
     *         400 Bad Request if the ID is invalid,
     *         or 404 Not Found if the entry does not exist.
     */
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