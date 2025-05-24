package com.example.lhv_homework.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private static final Set<String> STOP_WORDS = Set.of(
        "mr", "mrs", "ms", "miss", "dr", "prof", "sir",
        "the", "and", "of", "to", "a", "an", "jr", "sr"
    );

    public boolean isValidName(String name) {
        return name != null &&
           !name.trim().isEmpty() &&
           name.matches("^(?=.*[A-Za-z])[A-Za-z\\s-]{2,50}$"); // letters, space, dash, apostrophe, 2–50 chars, must contain at least one letter
    } 

    public boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    public boolean verifyName(String name) {
        if (!isValidName(name)) return false;

        String preprocessedName = preprocessName(name);
        return true;
    }

    public String normalizeName(String name) {
        return java.text.Normalizer
                .normalize(name, java.text.Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
    }

    public String cleanName(String name) {
        return name
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z\\s'-]", ""); // Keep only letters, spaces, apostrophes and dashes
    }

    public List<String> tokenizeName(String name) {
        List<String> tokens = Arrays.stream(name.split(" "))
                .filter(token -> !STOP_WORDS.contains(token))
                .distinct()
                .collect(Collectors.toList());

        Collections.sort(tokens);
        return tokens;
    }

    public String preprocessName(String rawName) {
        // Normalize accents and diacritics
        String normalized = normalizeName(rawName);

        // Lowercase and remove punctuation
        String cleaned = cleanName(normalized);

        // Tokenize
        List<String> tokens = tokenizeName(cleaned);

        // Join back into a single string
        return String.join(" ", tokens);
    }


}
