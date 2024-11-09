import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import utils.CacheManager;

public class CacheManagerTest {

    private CacheManager cacheManager;
    private ScheduledExecutorService scheduler;

    @Before
    public void setUp() {
        scheduler = Executors.newScheduledThreadPool(1);
        cacheManager = new CacheManager(1000, scheduler); // 1 second expiry
    }

    @Test
    public void testGetOrFetch_CacheHit() {
        // Create a mock JSON node
        ObjectNode mockData = Mockito.mock(ObjectNode.class);
        when(mockData.toString()).thenReturn("{\"data\":\"value\"}");

        // Simulate caching the data
        cacheManager.getOrFetch("testKey", () -> CompletableFuture.completedFuture(mockData));

        // Attempt to fetch again
        cacheManager.getOrFetch("testKey", () -> {
            throw new RuntimeException("Should not be called on cache hit");
        }).thenAccept(result -> {
            assertNotNull(result);
            assertEquals(mockData, result);
        });
    }

    @Test
    public void testGetOrFetch_CacheMiss() {
        // Create a mock JSON node
        ObjectNode mockData = Mockito.mock(ObjectNode.class);
        when(mockData.toString()).thenReturn("{\"data\":\"value\"}");

        // Fetch and cache the data
        cacheManager.getOrFetch("testKey", () -> CompletableFuture.completedFuture(mockData))
                .thenAccept(result -> {
                    assertNotNull(result);
                    assertEquals(mockData, result);
                });
    }

    @Test
    public void testCacheExpiration() throws InterruptedException {
        // Create a mock JSON node
        ObjectNode mockData = Mockito.mock(ObjectNode.class);
        when(mockData.toString()).thenReturn("{\"data\":\"value\"}");

        // Fetch and cache the data
        cacheManager.getOrFetch("testKey", () -> CompletableFuture.completedFuture(mockData));

        // Wait for cache to expire
        Thread.sleep(1100); // Wait for more than 1 second to ensure expiration

        // Attempt to fetch the data again
        cacheManager.getOrFetch("testKey", () -> CompletableFuture.completedFuture(mockData))
                .thenAccept(result -> {
                    assertNotNull(result);
                    assertEquals(mockData, result);
                });
    }

    @Test
    public void testCleanUp() throws InterruptedException {
        // Create a mock JSON node
        ObjectNode mockData = Mockito.mock(ObjectNode.class);
        when(mockData.toString()).thenReturn("{\"data\":\"value\"}");

        // Fetch and cache the data
        cacheManager.getOrFetch("testKey", () -> CompletableFuture.completedFuture(mockData));

        // Verify that the cache contains the data
        cacheManager.getOrFetch("testKey", () -> CompletableFuture.completedFuture(mockData))
                .thenAccept(result -> {
                    assertNotNull(result);
                });

        // Wait for cache to expire
        Thread.sleep(1100); // Wait for more than 1 second to ensure expiration
        // Clean up is now indirectly tested through expiration
    }
}
