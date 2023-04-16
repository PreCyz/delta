package pg.delta;

import org.ehcache.config.builders.*;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;

@Configuration
@EnableCaching
@EnableScheduling
public class MainConfig {

    @Bean
    public CacheManager ehCacheManager(
            @Value("${cache-names}") List<String> cacheNames,
            @Value("${cache-duration}") int cacheDuration,
            @Value("${cache-entries}") int cacheEntries) {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        CacheEventListenerConfigurationBuilder listenerConfiguration = CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(new CacheEventLogger(), EnumSet.allOf(EventType.class))
                .unordered()
                .asynchronous();

        CacheConfigurationBuilder<Object, Object> configuration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class,
                        Object.class,
                        //ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(2, MemoryUnit.MB))
                        ResourcePoolsBuilder.heap(cacheEntries))
                .withService(listenerConfiguration)
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(cacheDuration)));

        javax.cache.configuration.Configuration<Object, Object> longBigDecimalConfig =
                Eh107Configuration.fromEhcacheCacheConfiguration(configuration);

        for (String cacheName : cacheNames) {
            cacheManager.createCache(cacheName, longBigDecimalConfig);
        }
        return cacheManager;
    }

}
