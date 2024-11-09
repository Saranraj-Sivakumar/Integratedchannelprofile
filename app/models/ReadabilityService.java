package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReadabilityService {

    public ObjectNode addReadabilityScores(JsonNode json) {
        double avgFleschKincaidGrade = 0.0;
        double avgFleschReadingEase = 0.0;
        int count = 0;

        for (JsonNode item : json.get("items")) {
            String description = item.get("snippet").get("description").asText();
            double fkGrade = calculateFleschKincaidGrade(description);
            double readingEase = calculateFleschReadingEase(description);

            // Attach readability scores to each video description
            ((ObjectNode) item.get("snippet")).put("fkGrade", fkGrade);
            ((ObjectNode) item.get("snippet")).put("readingEase", readingEase);

            // Accumulate scores for average calculation
            avgFleschKincaidGrade += fkGrade;
            avgFleschReadingEase += readingEase;
            count++;
        }

        // Calculate the average readability scores
        avgFleschKincaidGrade = roundToTwoDecimals(avgFleschKincaidGrade / count);
        avgFleschReadingEase = roundToTwoDecimals(avgFleschReadingEase / count);

        // Add average readability scores to the response JSON
        ObjectNode resultWithAverages = json.deepCopy();
        resultWithAverages.put("avgFleschKincaidGrade", avgFleschKincaidGrade);
        resultWithAverages.put("avgFleschReadingEase", avgFleschReadingEase);
        return resultWithAverages;
    }

    public double calculateFleschKincaidGrade(String text) {
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int syllableCount = countSyllablesInText(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0;
        }

        double gradeLevel = 0.39 * ((double) wordCount / sentenceCount) + 11.8 * ((double) syllableCount / wordCount) - 15.59;
        return roundToTwoDecimals(gradeLevel);
    }

    public double calculateFleschReadingEase(String text) {
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int syllableCount = countSyllablesInText(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0;
        }

        double readingEase = 206.835 - 1.015 * ((double) wordCount / sentenceCount) - 84.6 * ((double) syllableCount / wordCount);
        return roundToTwoDecimals(readingEase);
    }

    public int countWords(String text) {
        String[] words = text.split("\\s+");
        return words.length;
    }

    public int countSentences(String text) {
        String[] sentences = text.split("[.!?]");
        return sentences.length;
    }

    public int countSyllablesInText(String text) {
        int syllableCount = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            syllableCount += countSyllables(word);
        }
        return syllableCount;
    }

    public int countSyllables(String word) {
        int count = 0;
        boolean lastWasVowel = false;
        String vowels = "aeiouy";
        word = word.toLowerCase();

        for (char wc : word.toCharArray()) {
            if (vowels.indexOf(wc) != -1) {
                if (!lastWasVowel) {
                    count++;
                }
                lastWasVowel = true;
            } else {
                lastWasVowel = false;
            }
        }

        if (word.endsWith("e")) {
            count--;
        }
        return Math.max(count, 1);
    }

    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
