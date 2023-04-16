package pg.delta;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.LoggerFactory;

class CacheEventLogger implements CacheEventListener<Object, Object> {
    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        LoggerFactory.getLogger(getClass())
                .debug("Cache event registered type [{}] key [{}], oldValue [{}], newValue [{}]",
                        cacheEvent.getType(), cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue()
                );
    }
}
