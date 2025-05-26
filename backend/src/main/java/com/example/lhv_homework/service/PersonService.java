package com.example.lhv_homework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lhv_homework.model.Person;
import com.example.lhv_homework.util.NameMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonService {

    @Autowired private RedisNameStore redisNameStore;

    private static final Set<String> STOP_WORDS = Set.of(
        "mr", "mrs", "ms", "miss", "dr", "prof", "sir",
        "the", "and", "of", "to", "a", "an", "jr", "sr"
    );

    /**
     * Validates a name based on character rules and length constraints.
     * Allows only letters, spaces, commas, periods, dashes, and apostrophes.
     *
     * @param name the input name string to validate.
     * @return true if the name is valid; false otherwise.
     */
    public boolean isValidName(String name) {
        return name != null &&
           !name.trim().isEmpty() &&
           name.matches("^(?=.*[A-Za-z])[A-Za-z\\s,.-]{2,50}$");    // allows only letters/spaces/dashes/commas/periods/apostrophes, 2–50 chars
                                                                    // must contain at least one letter
    } 

    /**
     * Checks if the provided ID is valid.
     * A valid ID is a non-null, positive number.
     *
     * @param id the ID to validate.
     * @return true if the ID is valid; false otherwise.
     */
    public boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * Verifies if a given name matches any sanctioned names in the Redis store.
     * It preprocesses the name and compares it against all sanctioned names using various similarity metrics.
     *
     * @param name the name to verify.
     * @return a map containing verification results, including whether the name is sanctioned,
     *         the matched sanctioned name, and similarity scores.
     */
    public Map<String, Object> verifyName(String name) {
        if (!isValidName(name)) return Map.of("isSanctioned", false, "msg", "Invalid name format");

        String preprocessedName = preprocessName(name);
        List<String> tokensName = Arrays.asList(preprocessedName.split(" "));
        List<Person> sanctionedPeople = redisNameStore.getAllNames();
        
        for (Person person : sanctionedPeople) {
            String preprocessedSanctioned = person.getPreprocessedName();
            List<String> tokensSanctioned = Arrays.asList(preprocessedSanctioned.split(" "));

            double jaroSimilarity = NameMatcher.jaroWinklerSimilarity(preprocessedName, preprocessedSanctioned);
            double jaccard = NameMatcher.jaccardSimilarity(preprocessedName, preprocessedSanctioned);
            int phoneticMatches = NameMatcher.phoneticMatches(tokensName, tokensSanctioned);
            double levenshteinNorm = NameMatcher.normalizedLevenshtein(preprocessedName, preprocessedSanctioned);

            if ((jaroSimilarity >= 0.90 && jaccard > 0.6) || phoneticMatches >= 2 || levenshteinNorm >= 0.85) {
                return Map.of(
                    "isSanctioned", true,
                    "sanctionedName", person.getName(),
                    "jaro", jaroSimilarity,
                    "jaccard", jaccard,
                    "phoneticMatches", phoneticMatches,
                    "levenshteinNorm", levenshteinNorm
                );
            }
        }
        return Map.of("isSanctioned", false, "msg", "Name not found in sanctioned list");
    }

    /**
     * Normalizes a name by removing accents and diacritics.
     * This is useful for standardizing names for comparison.
     *
     * @param name the input name to normalize.
     * @return the normalized name without accents or diacritics.
     */
    public String normalizeName(String name) {
        return java.text.Normalizer
                .normalize(name, java.text.Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
    }

    /**
     * Cleans a name by trimming whitespace, converting to lowercase,
     * and removing unwanted characters.
     *
     * @param name the input name to clean.
     * @return the cleaned name string.
     */
    public String cleanName(String name) {
        return name
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z\\s'-]", ""); // Keep only letters, spaces, apostrophes and dashes
    }

    /**
     * Tokenizes a name by splitting it into distinct words,
     * filtering out common stop words, and sorting the tokens.
     *
     * @param name the input name to tokenize.
     * @return a sorted list of distinct tokens from the name.
     */
    public List<String> tokenizeName(String name) {
        List<String> tokens = Arrays.stream(name.split(" "))
                .filter(token -> !STOP_WORDS.contains(token))
                .distinct()
                .collect(Collectors.toList());

        Collections.sort(tokens);
        return tokens;
    }

    /**
     * Applies full preprocessing to a raw name string.
     * Steps include normalization, cleaning, tokenization, and reassembly.
     *
     * @param rawName the raw input name to preprocess.
     * @return a preprocessed name string suitable for matching.
     */
    public String preprocessName(String rawName) {
        String normalized = normalizeName(rawName);
        String cleaned = cleanName(normalized);
        List<String> tokens = tokenizeName(cleaned);

        return String.join(" ", tokens);
    }
}
