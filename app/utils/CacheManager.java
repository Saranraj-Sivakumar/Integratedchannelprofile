package utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class CacheManager {

    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();
    private final long cacheExpiry; // Cache expiry time in milliseconds
    private final ScheduledExecutorService scheduler;

    public CacheManager(long cacheExpiry, ScheduledExecutorService scheduler) {
        this.cacheExpiry = cacheExpiry;
        this.scheduler = scheduler;

        // Schedule a task to clean up expired cache entries at fixed intervals
        scheduler.scheduleAtFixedRate(this::cleanUp, cacheExpiry, cacheExpiry, TimeUnit.MILLISECONDS);
    }

    // Retrieves the cached result or fetches and caches a new one if not found
    public CompletionStage<JsonNode> getOrFetch(String query, Supplier<CompletionStage<JsonNode>> fetchFunction) {
        CachedResult cachedResult = cache.get(query);

        // Check if cached result exists and is still valid
        if (cachedResult != null && !cachedResult.isExpired()) {
            System.out.println("[CacheManager] Cache hit for query: " + query); // Debug statement for cache hit
            return CompletableFuture.completedFuture(cachedResult.getData());
        }

        // Cache miss - fetch new data, cache it, and return it
        System.out.println("[CacheManager] Cache miss for query: " + query); // Debug statement for cache miss
        CompletionStage<JsonNode> future = fetchFunction.get();
        future.thenAccept(data -> {
            cache.put(query, new CachedResult(data, System.currentTimeMillis() + cacheExpiry));
            System.out.println("[CacheManager] Cached new data for query: " + query); // Debug statement for caching new data
        });
        return future;
    }

    // Removes expired cache entries
    public void cleanUp() {
        long currentTime = System.currentTimeMillis();
        int initialCacheSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));

        int removedEntries = initialCacheSize - cache.size();
        if (removedEntries > 0) {
            System.out.println("[CacheManager] Cleaned up " + removedEntries + " expired cache entries."); // Debug statement for cache clean-up
        }
    }

    // Inner class to store cached data and expiration time
    public static class CachedResult {
        private final JsonNode data;
        private final long expiryTime;

        public CachedResult(JsonNode data, long expiryTime) {
            this.data = data;
            this.expiryTime = expiryTime;
        }

        public JsonNode getData() {
            return data;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        public boolean isExpired(long currentTime) {
            return currentTime > expiryTime;
        }
    }
}
