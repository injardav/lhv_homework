package com.example.lhv_homework;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.example.lhv_homework.service.PersonService;
import com.example.lhv_homework.service.RedisNameStore;
import com.example.lhv_homework.util.NameMatcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class LhvHomeworkApplicationTests {

	@Autowired private MockMvc mockMvc;
    @Autowired private RedisNameStore redisNameStore;
    @Autowired private PersonService personService;

    private Long testPersonId;

    @BeforeEach
    void setup() {
        testPersonId = redisNameStore.saveSanctionedName("Osama Bin Laden", personService.preprocessName("Osama Bin Laden"));
    }

    @AfterEach
    void teardown() {
        redisNameStore.deleteSanctionedEntry(testPersonId.toString());
    }

    private ResultActions verifyName(String name) throws Exception {
        return mockMvc.perform(post("/api/v1/names/verify")
                .contentType("text/plain")
                .content(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Osama Bin Laden",
            "Ben Osama Ladn",
            "Laden Osama Bin",
            "to the Mr. Osama Bin Laden"
    })
    void testSanctionedNameVariants(String variant) throws Exception {
        verifyName(variant)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSanctioned").value(true))
                .andExpect(jsonPath("$.sanctionedName").value("Osama Bin Laden"));
    }

    @Test
    void testNonMatchReturnsFalse() throws Exception {
        mockMvc.perform(post("/api/v1/names/verify")
                .contentType("text/plain")
                .content("John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSanctioned").value(false))
                .andExpect(jsonPath("$.msg").value("Name not found in sanctioned list"));
    }

    @Test
    void testEmptyNameReturnsBadRequest() throws Exception {
        verifyName("")
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNullNameIsHandled() throws Exception {
        mockMvc.perform(post("/api/v1/names/verify"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePerson() throws Exception {
        String newName = "Jane Doe";
        MvcResult result = mockMvc.perform(post("/api/v1/names")
                        .contentType("text/plain")
                        .content(newName))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.preprocessedName").value(personService.preprocessName(newName)))
                .andReturn();

        // Parse ID safely
        String response = result.getResponse().getContentAsString();
        JsonNode json = new ObjectMapper().readTree(response);
        String id = json.get("id").asText();

        // Clean up
        redisNameStore.deleteSanctionedEntry(id);
    }

    @Test
    void testGetAllNames() throws Exception {
        mockMvc.perform(get("/api/v1/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Osama Bin Laden"))
                .andExpect(jsonPath("$[0].preprocessedName").value(personService.preprocessName("Osama Bin Laden")));
    }

    @Test
    void testGetPersonById() throws Exception {
        mockMvc.perform(get("/api/v1/names/" + testPersonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Osama Bin Laden"))
                .andExpect(jsonPath("$.preprocessedName").value(personService.preprocessName("Osama Bin Laden")));
    }

    @Test
    void testUpdatePerson() throws Exception {
        String updatedName = "Osama Bin Laden Updated";
        mockMvc.perform(put("/api/v1/names/" + testPersonId)
                .contentType("text/plain")
                .content(updatedName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.preprocessedName").value(personService.preprocessName(updatedName)));
    }

    @Test
    void testDeletePerson() throws Exception {
        mockMvc.perform(delete("/api/v1/names/" + testPersonId))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/v1/names/" + testPersonId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteNonExistentPerson() throws Exception {
        mockMvc.perform(delete("/api/v1/names/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testNamePreprocessing() {
        assertEquals("bin laden osama", personService.preprocessName("Mr. Osama Bin Laden"));
        assertEquals("bin laden", personService.preprocessName("to the bin laden"));
        assertEquals("osama", personService.preprocessName("  Osama  "));
        assertEquals("bin laden osama", personService.preprocessName("Osámá Bín Läden"));
    }

    @Test
    void testStringSimilarityMetrics() {
        double levenshteinNorm = NameMatcher.normalizedLevenshtein("osama", "asama");
        assertTrue(levenshteinNorm >= 0.8, "Expected Levenshtein >= 0.8 for osama vs asama");

        double jaroSim = NameMatcher.jaroWinklerSimilarity("bin laden", "bin ladn");
        assertTrue(jaroSim >= 0.9, "Expected Jaro-Winkler >= 0.9 for bin laden vs bin ladn");

        double jaccard = NameMatcher.jaccardSimilarity("bin laden", "laden osama");
        assertEquals(1.0 / 3, jaccard, 0.01, "Expected Jaccard ~ 0.33 for ['bin','laden'] vs ['laden','osama']");

        int phoneticMatches = NameMatcher.phoneticMatches(
                List.of("osama", "laden"),
                List.of("asoma", "ladn")
        );
        assertEquals(2, phoneticMatches, "Expected 2 phonetic matches for osama/laden vs asoma/ladn");
    }
}
