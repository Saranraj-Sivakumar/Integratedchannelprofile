import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import models.ReadabilityService;
import static org.junit.Assert.*;
import play.libs.Json;

public class ReadabilityServiceTest {

    private ReadabilityService readabilityService;

    @Before
    public void setUp() {
        readabilityService = new ReadabilityService();
    }

    @Test
    public void testAddReadabilityScores() {
        // Mock input JSON
        String jsonString = "{\"items\": [" +
                "{\"snippet\": {\"description\": \"Video1 description\"}}, " +
                "{\"snippet\": {\"description\": \"Video2 description\"}}" +
                "]}";
        JsonNode inputJson = Json.parse(jsonString);

        // Call the method
        ObjectNode result = readabilityService.addReadabilityScores(inputJson);

        // Validate the scores were added
        assertNotNull(result);
        assertEquals("Video1 description", result.get("items").get(0).get("snippet").get("description").asText());
        assertTrue(result.get("items").get(0).get("snippet").has("fkGrade"));
        assertTrue(result.get("items").get(0).get("snippet").has("readingEase"));

        // Check averages
        assertTrue(result.has("avgFleschKincaidGrade"));
        assertTrue(result.has("avgFleschReadingEase"));
    }

    @Test
    public void testCalculateFleschKincaidGrade() {
        double grade = readabilityService.calculateFleschKincaidGrade("This is a simple test sentence.");
        assertTrue(grade >= 0); // Ensure it returns a non-negative grade
    }

    @Test
    public void testCalculateFleschReadingEase() {
        double ease = readabilityService.calculateFleschReadingEase("This is a simple test sentence.");
        assertTrue(ease >= 0); // Ensure it returns a non-negative ease score
    }

    @Test
    public void testCountWords() {
        int wordCount = readabilityService.countWords("This is a test.");
        assertEquals(4, wordCount);
    }

    @Test
    public void testCountSentences() {
        int sentenceCount = readabilityService.countSentences("This is the first sentence. This is the second.");
        assertEquals(2, sentenceCount);
    }

    @Test
    public void testCountSyllablesInText() {
        int syllableCount = readabilityService.countSyllablesInText("This is a test sentence.");
        assertTrue(syllableCount > 0); // Ensure it counts some syllables
    }

    @Test
    public void testCountSyllables() {
        int count = readabilityService.countSyllables("test");
        assertEquals(1, count);

        count = readabilityService.countSyllables("beautiful");
        assertEquals(3, count);
    }

    @Test
    public void testRoundToTwoDecimals() {
        double roundedValue = readabilityService.roundToTwoDecimals(3.14159);
        assertEquals(3.14, roundedValue, 0.01); // Allow a small delta
    }
}
