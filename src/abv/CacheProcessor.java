package abv;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/*
The cache has next logic - we have a cache object with an expiration time. When we put an object in memory cache
we are setting expiration time accordingly our configuration. If the memory cache is full we put object in the disk cache
if it turns on. Each second auxiliary thread checks the memory cache and removes expired objects. Also each timesBeforeCleanup
times it checks disk cache and cleans it if unsuccessful counts of tries to write objects in this cache more than
size of disk cache * diskCacheCleanupFactor / 100. In this case disk cache costs as invalid and resets.
 */

public class CacheProcessor<K, V> {

    private Map <K, HMCacheObject<K, V>> cacheHashMap = new ConcurrentHashMap<>();
    private DiskCache<K, V> diskCache;
    private int maxObjectsInMemoryCache;
    private final int timesBeforeCleanup = 10;
    private int timeToLiveInMemory;


    public CacheProcessor(int maxObjectsInMemoryCache, int maxObjectsDiskCache, boolean isDiskCacheEnabled, int diskCacheCleanupFactor,
                          int memoryObjectsLifetime, int diskObjectsLifetime) {

        this.maxObjectsInMemoryCache = maxObjectsInMemoryCache;
        this.timeToLiveInMemory = memoryObjectsLifetime;


        if (isDiskCacheEnabled) {
            diskCache = new DiskCacheReal<>(maxObjectsDiskCache, diskCacheCleanupFactor, diskObjectsLifetime);
        } else {
            diskCache = new DiskCacheMock<>();
        }

        Thread cleanupThread = new Thread(() -> {
            int cleanupCount = 0;
            while (true) {
                try {
                    cleanup();
                    if (cleanupCount % timesBeforeCleanup == 0) {

                        diskCache.cleanup();
                        cleanupCount = 0;
                    }
                    Thread.sleep(1000);
                    cleanupCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        );
        cleanupThread.setDaemon(true);
        cleanupThread.start();

    }

    public void put(K id, V value) {
        if (cacheHashMap.size() == maxObjectsInMemoryCache) {
            diskCache.put(id, value);
        } else {
            cacheHashMap.put(id, new HMCacheObject(value, id, timeToLiveInMemory));
        }
    }

    public V get(K identifier) {
        HMCacheObject<K, V> object = cacheHashMap.get(identifier);
        if (object == null) {
            return tryToFindInDiskCache(identifier);
        } else {
            object.refreshExpiration();
            return object.object;
        }
    }

    private V tryToFindInDiskCache(K identifier) {
        V object = null;
            object = diskCache.get(identifier);
        return object;
    }

    private void cleanup() {

        cacheHashMap.values().forEach(item -> {
            if (item.isExpired()) {
                cacheHashMap.remove(item.getIdentifier());
            }
        });
    }

}
