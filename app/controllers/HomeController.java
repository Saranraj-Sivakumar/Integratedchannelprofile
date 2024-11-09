package controllers;

import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import models.YouTubeService;
import models.SubmissionSentimentService;
import models.ChannelProfileService;
import utils.CacheManager;
import utils.SessionManager;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import java.util.List;
import views.html.channelProfile;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;

public class HomeController extends Controller {

    private final YouTubeService youTubeService;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;
    private final SubmissionSentimentService sentimentService;
    private final ChannelProfileService channelProfileService;

    @Inject
    public HomeController(YouTubeService youTubeService, CacheManager cacheManager, SessionManager sessionManager,
                          SubmissionSentimentService sentimentService, ChannelProfileService channelProfileService) {
        this.youTubeService = youTubeService;
        this.cacheManager = cacheManager;
        this.sessionManager = sessionManager;
        this.sentimentService = sentimentService;
        this.channelProfileService = channelProfileService;
    }

    // Endpoint for searching videos
    public CompletionStage<Result> search(Http.Request request, String query) {
        return cacheManager.getOrFetch(query, () -> youTubeService.fetchVideosWithReadabilityScores(query))
                .thenApply(result -> {
                    JsonNode resultWithSentiment = sentimentService.addSentimentScores(result);
                    String updatedSessionData = sessionManager.prepareSessionData(request.session(), query);
                    if (updatedSessionData == null) {
                        updatedSessionData = "[]";
                    }

                    return ok(play.libs.Json.toJson(resultWithSentiment)).withSession(
                            request.session().adding("searchResults", updatedSessionData)
                    );
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError("An error occurred while processing the search request.");
                });
    }

    public CompletionStage<Result> channelProfile(String channelId) {
        return channelProfileService.fetchChannelProfile(channelId).thenCombine(
                youTubeService.fetchChannelVideos(channelId),
                (profileJson, videosJson) -> {
                    JsonNode channelData = profileJson.path("items").get(0);
                    JsonNode statistics = channelData.path("statistics");
                    JsonNode snippet = channelData.path("snippet");

                    List<JsonNode> videosList = new ArrayList<>();
                    for (JsonNode item : videosJson.path("items")) {
                        videosList.add(item);
                    }

                    return ok(views.html.channelProfile.render(channelData, videosList));
                }
        ).exceptionally(ex -> {
            ex.printStackTrace();
            return internalServerError("An error occurred while fetching the channel profile.");
        });
    }

    // Index page
    public Result index() {
        return ok(views.html.index.render("TubeLytics - YouTube Search"));
    }

    // Endpoint to get search history
    public Result getSearchHistory(Http.Request request) {
        JsonNode searchHistoryJson = play.libs.Json.toJson(sessionManager.getSearchHistory(request.session()));
        return ok(searchHistoryJson);
    }
}
