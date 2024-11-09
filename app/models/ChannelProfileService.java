package models;

import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ChannelProfileService {

    private final YouTubeService youTubeService;

    @Inject
    public ChannelProfileService(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public CompletionStage<JsonNode> fetchChannelProfile(String channelId) {
        return youTubeService.fetchChannelProfile(channelId).thenApply(profileJson -> {
            if (profileJson != null && profileJson.has("items") && profileJson.path("items").size() > 0) {
                return profileJson;
            }
            return profileJson;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
