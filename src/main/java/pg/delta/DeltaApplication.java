package pg.delta;

import org.ehcache.config.builders.*;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import pg.delta.cache.CacheEventLogger;
import pg.delta.model.*;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;

@SpringBootApplication
public class DeltaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeltaApplication.class, args);
    }

    @Configuration
    @EnableCaching
    @EnableScheduling
    static class MainConfig {

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

            javax.cache.configuration.Configuration<Object, Object> cacheConfig =
                    Eh107Configuration.fromEhcacheCacheConfiguration(configuration);

            for (String cacheName : cacheNames) {
                cacheManager.createCache(cacheName, cacheConfig);
            }
            return cacheManager;
        }

        @Bean
        public ModelRepository modelRepository() {
            return new ModelRepository();
        }

        @Bean
        public ModelService modelService(ModelRepository modelRepository) {
            return new ModelService(modelRepository);
        }

        @Bean
        public DeltaScheduler deltaScheduler(ModelRepository modelRepository, CacheManager ehCacheManager) {
            return new DeltaScheduler(modelRepository, ehCacheManager);

        }
    }

    @Component
    public static class ApplicationStartingListener implements ApplicationListener<ApplicationStartingEvent> {
        @Override
        public void onApplicationEvent(ApplicationStartingEvent event) {
            String msg = String.format("I'm starting %s",
                    LocalDateTime.ofEpochSecond(event.getTimestamp(), 0, ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            LoggerFactory.getLogger(ApplicationStartingListener.class).info(msg);
        }
    }

    @Component
    public static class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            double l = event.getTimeTaken().toMillis() / 1000d;
            String msg = String.format("I'm started in %1.3f seconds%n", l);
            LoggerFactory.getLogger(ApplicationStartedListener.class).info(msg);
        }
    }

    @Component
    public static class ApplicationContextInitializedListener implements ApplicationListener<ApplicationContextInitializedEvent> {
        @Override
        public void onApplicationEvent(ApplicationContextInitializedEvent event) {
            LoggerFactory.getLogger(ApplicationStartingListener.class).info("Application context initialized");
        }
    }

    @Component
    public static class ApplicationEnvironmentListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
        @Override
        public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
            LoggerFactory.getLogger(ApplicationStartingListener.class).info("Application environment prepared");
        }
    }

    @Component
    public static class ApplicationPreparedListener implements ApplicationListener<ApplicationPreparedEvent> {
        @Override
        public void onApplicationEvent(ApplicationPreparedEvent event) {
            LoggerFactory.getLogger(ApplicationStartingListener.class).info("Application prepared");
        }
    }

    @Component
    public static class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
        private final DeltaScheduler deltaScheduler;

        public ApplicationReadyListener(DeltaScheduler deltaScheduler) {
            this.deltaScheduler = deltaScheduler;
        }

        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            LoggerFactory.getLogger(ApplicationStartingListener.class).info("Application ready");
            deltaScheduler.generateDelta();
        }
    }

    @Component
    public static class ApplicationFailedListener implements ApplicationListener<ApplicationFailedEvent> {
        @Override
        public void onApplicationEvent(ApplicationFailedEvent event) {
            LoggerFactory.getLogger(ApplicationStartingListener.class).info("Application failed");
        }
    }

}
