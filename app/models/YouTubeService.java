package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class YouTubeService {

    private final String apiKey;
    private final HttpClient httpClient;
    private final ReadabilityService readabilityService;
    private final SubmissionSentimentService sentimentService;

    @Inject
    public YouTubeService(Config config, ReadabilityService readabilityService, SubmissionSentimentService sentimentService) {
        this.apiKey = config.getString("youtube.api.key");
        this.httpClient = HttpClient.newHttpClient();
        this.readabilityService = readabilityService;
        this.sentimentService = sentimentService;
    }

    // Fetch videos and add readability scores
    public CompletionStage<JsonNode> fetchVideosWithReadabilityScores(String query) {
        return fetchVideos(query).thenCompose(videosJson ->
                CompletableFuture.supplyAsync(() -> readabilityService.addReadabilityScores(videosJson))
        );
    }

    // Fetch video details from YouTube API
    public CompletionStage<JsonNode> fetchVideos(String query) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=" + query + "&key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            return mapper.readTree(response.body());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    // Fetch channel profile details
    public CompletionStage<JsonNode> fetchChannelProfile(String channelId) {
        String url = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" + channelId + "&key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readTree(response.body());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    // Fetch videos from the channel
    public CompletionStage<JsonNode> fetchChannelVideos(String channelId) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=" + channelId + "&maxResults=10&key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            return mapper.readTree(response.body());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }
}
