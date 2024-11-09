package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class SubmissionSentimentService {

    private static final List<String> HAPPY_WORDS = Arrays.asList(
            "happy", "joy", "excited", "love", "smile", "fun", "laugh", "cheerful", "fantastic", "elated",
            "content", "blessed", "hopeful", "grateful", "optimistic", "jovial", "radiant", "ecstatic", "wonderful",
            "joyful", "delightful", "positive", "bright", "good", "peaceful", "bubbly", "gleeful", "uplifted",
            "successful", "enthusiastic", "thrilled", "elation", "playful", "amazing",
            "kid", "child", "baby", "kitty", "cat", "dog", "puppy", "food", "delicious", "yummy", "tasty",
            "travel", "trip", "vacation", "holiday", "explore", "adventure", "journey", "wanderlust", "excursion"
    );

    private static final List<String> SAD_WORDS = Arrays.asList(
            "sad", "angry", "upset", "hate", "cry", "depressed", "gloomy", "down", "broken", "hopeless",
            "lonely", "despair", "mournful", "melancholy", "heartbroken", "miserable", "sorrowful", "empty",
            "lost", "bitter", "downhearted", "troubled", "grief", "abandoned", "desolate", "isolated", "stressed",
            "painful", "wretched", "grief-stricken", "disheartened", "unhappy", "unsettled", "disappointed",
            "death", "lost", "nope", "mourning", "grief", "funeral", "widow", "widower", "hopeless"
    );

    private static final List<String> HAPPY_EMOJIS = Arrays.asList(
            ":-)", ":)", "😊", "😁", "😃", "😀", "😄", "😍", "😎", "🤩", "😋", "😻", "🎉", "🥳", "🤗", "💖", "🌟", "🥰"
    );

    private static final List<String> SAD_EMOJIS = Arrays.asList(
            ":-(", ":(", "😞", "😢", "😔", "🙁", "😓", "😭", "😖", "😩", "😤", "😿", "💔", "☹️", "😣", "🥀", "😫", "😩"
    );

    @Inject
    public SubmissionSentimentService() {
    }

    public JsonNode addSentimentScores(JsonNode videosJson) {
        int totalHappyCount = 0;
        int totalSadCount = 0;
        int totalNeutralCount = 0;

        for (JsonNode item : videosJson.path("items")) {
            String description = item.path("snippet").path("description").asText();
            String sentiment = analyzeSentiment(description);

            // Remove the print statements for sentiment analysis details
            ((ObjectNode) item).put("sentiment", sentiment);

            if (sentiment.equals("1")) {
                totalHappyCount++;
            } else if (sentiment.equals("0")) {
                totalSadCount++;
            } else {
                totalNeutralCount++;
            }
        }

        String averageSentiment = getFinalSentiment(totalHappyCount, totalSadCount, totalNeutralCount);
        ObjectNode result = ((ObjectNode) videosJson);
        result.put("averageSentiment", averageSentiment);

        // Return the updated JSON without printing to the console
        return result;
    }

    private String analyzeSentiment(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "1"; // Happy
        }
        int happyCount = countHappyWordsAndEmojis(description);
        int sadCount = countSadWordsAndEmojis(description);
        int totalCount = happyCount + sadCount;

        if (totalCount == 0) {
            return "2"; // Neutral
        }

        double happyPercentage = (double) happyCount / totalCount * 100;
        double sadPercentage = (double) sadCount / totalCount * 100;

        if (happyPercentage >= 70) {
            return "1"; // Happy
        } else if (sadPercentage >= 70) {
            return "0"; // Sad
        } else {
            return "2"; // Neutral
        }
    }

    private String getFinalSentiment(int happyCount, int sadCount, int neutralCount) {
        int total = happyCount + sadCount + neutralCount;

        if (total == 0) {
            return ":-|";  // Neutral
        }

        double happyPercentage = (double) happyCount / total * 100;
        double sadPercentage = (double) sadCount / total * 100;
        double neutralPercentage = (double) neutralCount / total * 100;

        if (happyPercentage > sadPercentage && happyPercentage > neutralPercentage) {
            return ":-)";  // Happy
        } else if (sadPercentage > happyPercentage && sadPercentage > neutralPercentage) {
            return ":-(";  // Sad
        } else {
            return ":-|";  // Neutral
        }
    }

    private int countHappyWordsAndEmojis(String description) {
        return countWordsAndEmojis(description, HAPPY_WORDS, HAPPY_EMOJIS);
    }

    private int countSadWordsAndEmojis(String description) {
        return countWordsAndEmojis(description, SAD_WORDS, SAD_EMOJIS);
    }

    private int countWordsAndEmojis(String description, List<String> words, List<String> emojis) {
        int wordCount = 0;
        for (String word : words) {
            wordCount += description.toLowerCase().split("\\b" + word.toLowerCase() + "\\b").length - 1;
        }
        int emojiCount = 0;
        for (String emoji : emojis) {
            emojiCount += description.length() - description.replace(emoji, "").length();
        }
        return wordCount + emojiCount;
    }
}
