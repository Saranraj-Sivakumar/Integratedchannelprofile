import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;

import com.fasterxml.jackson.databind.JsonNode;
import models.YouTubeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.CacheManager;
import utils.SessionManager;
import controllers.HomeController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HomeControllerTest {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private SessionManager sessionManager;

    @InjectMocks
    private HomeController homeController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSearch_successful() {

        // Mocking a valid API response that YouTube might return
        JsonNode mockJsonResult = Json.parse("{\"items\": [" +
                "{\"snippet\": {\"description\": \"Video1 description\"}}, " +
                "{\"snippet\": {\"description\": \"Video2 description\"}}" +
                "]}");

        // Mock responses from CacheManager and YouTubeService
        when(cacheManager.getOrFetch(anyString(), any())).thenReturn(CompletableFuture.completedFuture(mockJsonResult));
        when(youTubeService.fetchVideosWithReadabilityScores(anyString())).thenReturn(CompletableFuture.completedFuture(mockJsonResult));

        // Mocking session history to return a valid list
        List<String> mockHistory = List.of("query1", "query2");
        when(sessionManager.getSearchHistory(any())).thenReturn(mockHistory);

        Http.Request request = fakeRequest().session("SESSION_RESULTS_KEY", "").build();

        CompletionStage<Result> resultStage = homeController.search(request, "test query");
        Result result = resultStage.toCompletableFuture().join();

        // Assert that the response is OK and contains JSON
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        JsonNode jsonResponse = Json.parse(content);

        // Directly accessing the items array without checking
        String firstDescription = jsonResponse.get("items").get(0).get("snippet").get("description").asText();
        assertEquals("Video1 description", firstDescription);

        String secondDescription = jsonResponse.get("items").get(1).get("snippet").get("description").asText();
        assertEquals("Video2 description", secondDescription);
    }

    @Test
    public void testSearch_withException() {
        when(cacheManager.getOrFetch(anyString(), any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

        Http.Request request = fakeRequest().session("SESSION_RESULTS_KEY", "").build();

        CompletionStage<Result> resultStage = homeController.search(request, "test query");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertEquals("An error occurred while processing the search request.", contentAsString(result));
    }

    @Test
    public void testGetSearchHistory() {
        // Set up mock behavior for getSearchHistory
        List<String> mockHistory = List.of("query1", "query2");
        when(sessionManager.getSearchHistory(any())).thenReturn(mockHistory);

        // Create a mock request
        Http.Request request = fakeRequest().session("SESSION_RESULTS_KEY", "").build();

        // Call the method and verify results
        Result result = homeController.getSearchHistory(request);
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        JsonNode jsonResponse = Json.parse(content);
        assertTrue(jsonResponse.isArray());
        assertEquals(2, jsonResponse.size());
        assertEquals("query1", jsonResponse.get(0).asText());
        assertEquals("query2", jsonResponse.get(1).asText());
    }

    @Test
    public void testIndex() {
        Result result = homeController.index();

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("TubeLytics - YouTube Search"));
    }
}
