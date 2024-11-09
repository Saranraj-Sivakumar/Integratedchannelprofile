import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import utils.JsonUtils;  // Importing the JsonUtils class

public class JsonUtilsTest {

    @Before
    public void setUp() {
        // Any setup needed before tests
        System.out.println("Setting up the tests...");
    }

    @Test
    public void testSerializeToString() {
        TestObject testObject = new TestObject("test", 123);
        // Use JsonUtils to serialize the object
        String json = JsonUtils.serializeToString(testObject);
        System.out.println("Serialized JSON: " + json);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test\""));  // Check if name is present
        assertTrue(json.contains("\"value\":123"));      // Check if value is present
    }

    @Test
    public void testDeserializeToJsonNodeList() {
        String json = "[{\"name\":\"test1\"}, {\"name\":\"test2\"}]";
        System.out.println("Deserializing JSON: " + json);

        List<JsonNode> nodes = JsonUtils.deserializeToJsonNodeList(json);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("test1", nodes.get(0).get("name").asText());
        assertEquals("test2", nodes.get(1).get("name").asText());
    }

    @Test
    public void testDeserializeToStringList() {
        String json = "[\"string1\", \"string2\"]";
        System.out.println("Deserializing string list JSON: " + json);

        List<String> strings = JsonUtils.deserializeToStringList(json);
        assertNotNull(strings);
        assertEquals(2, strings.size());
        assertEquals("string1", strings.get(0));
        assertEquals("string2", strings.get(1));
    }

    @Test
    public void testDeserializeToJsonNodeList_ErrorHandling() {
        // Testing deserialization of invalid JSON
        String invalidJson = "invalid json";
        System.out.println("Testing deserialization of invalid JSON: " + invalidJson);

        List<JsonNode> nodes = JsonUtils.deserializeToJsonNodeList(invalidJson);
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty()); // Expect an empty list
    }

    @Test
    public void testDeserializeToStringList_ErrorHandling() {
        // Testing deserialization of invalid JSON
        String invalidJson = "invalid json";
        System.out.println("Testing deserialization of invalid JSON: " + invalidJson);

        List<String> strings = JsonUtils.deserializeToStringList(invalidJson);
        assertNotNull(strings);
        assertTrue(strings.isEmpty()); // Expect an empty list
    }

    // Helper class for testing serialization
    private static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        // Getters for serialization
        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
