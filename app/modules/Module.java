package modules;

import com.google.inject.AbstractModule;
import utils.CacheManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import utils.SessionManager;
import utils.JsonUtils;


public class Module extends AbstractModule {
    @Override
    protected void configure() {
        // Bind CacheManager as a singleton with a custom expiration and scheduler
        long cacheExpiry = 10000L; // 10 seconds for demonstration
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        bind(CacheManager.class).toInstance(new CacheManager(cacheExpiry, scheduler));
        bind(SessionManager.class).toInstance(new SessionManager(new JsonUtils()));
    }
}
