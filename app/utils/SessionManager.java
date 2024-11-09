package utils;

import play.mvc.Http;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import java.util.Optional;

public class SessionManager {

    public static final String SESSION_RESULTS_KEY = "searchResults";
    public static final int MAX_SEARCH_HISTORY = 10;
    public final JsonUtils jsonUtils; // Assume JsonUtils is injectable

    @Inject
    public SessionManager(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    public String getSessionResultsKey() {
        return SESSION_RESULTS_KEY;
    }

    public String prepareSessionData(Http.Session session, String query) {
        List<String> searchHistory = getSearchHistory(session);
        searchHistory.add(0, query); // Store only the search term

        if (searchHistory.size() > MAX_SEARCH_HISTORY) {
            searchHistory.remove(searchHistory.size() - 1); // Keep history within max limit
        }

        String serializedData = jsonUtils.serializeToString(searchHistory);

        // Add a debug print statement
        System.out.println("Serialized session data: " + serializedData);

        return serializedData;  // Ensure this is never null
    }

    // Get the search history for the current session, expecting a list of strings
    public List<String> getSearchHistory(Http.Session session) {
        // Use Optional to retrieve the session value and provide a default if absent
        String serializedResults = session.get(SESSION_RESULTS_KEY).orElse("[]");
        return jsonUtils.deserializeToStringList(serializedResults);
    }


}
