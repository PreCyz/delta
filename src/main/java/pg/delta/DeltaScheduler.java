package pg.delta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class DeltaScheduler {
    private final Logger log;
    private final ModelRepository modelRepository;
    private final CacheManager ehCacheManager;

    public DeltaScheduler(ModelRepository modelRepository, CacheManager ehCacheManager) {
        this.modelRepository = modelRepository;
        this.ehCacheManager = ehCacheManager;
        log = LoggerFactory.getLogger(getClass());
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 5L, initialDelay = 2L)
    public void generateDelta() {
        Map<LocalDate, Model> delta = modelRepository.generateDelta(LocalDate.now().minusDays(30));
        Cache<Object, Object> deltaCache = ehCacheManager.getCache("deltaCache");
        deltaCache.clear();

        for (Map.Entry<LocalDate, Model> entry : delta.entrySet()) {
            Predicate<Model> cachePredicate = it -> it.changeDateTime().toLocalDate().isAfter(entry.getKey()) ||
                    it.changeDateTime().toLocalDate().isEqual(entry.getKey());

            deltaCache.put(
                    entry.getKey(),
                    delta.values()
                            .stream()
                            .filter(cachePredicate)
                            .toList()
            );
        }

        Iterator<Cache.Entry<Object, Object>> iterator = deltaCache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry<Object, Object> entry = iterator.next();
            log.debug("Key: {}, Value {}", entry.getKey(), entry.getValue());
            log.debug("DeltaValue {}", delta.get(entry.getKey()));
        }
    }
}
