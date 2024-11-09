package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serializeToString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static List<JsonNode> deserializeToJsonNodeList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // New method specifically for deserializing a list of strings
    public static List<String> deserializeToStringList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
