package abv;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by user on 15/04/16.
 */
public class DiskCacheReal<K, V> implements DiskCache<K, V> {

    private static String fileName = "cache.dat";
    private static int objectsInCache;
    private static int maxObjectsInCache;
    private static int diskCacheCleanupFactor;
    private static int secondToLive;
    private static int overflowCounter;

    public DiskCacheReal(int maxObjectsInCache, int diskCacheCleanupFactor, int secondToLive) {
        this.maxObjectsInCache = maxObjectsInCache;
        this.diskCacheCleanupFactor = diskCacheCleanupFactor;
        this.secondToLive = secondToLive;
        resetCacheFile();
    }

    @Override
    public V get(K id) {

        HMCacheObject<K, V> o;
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(fileName));
            for (int i = 0; i < objectsInCache; i++) {
                o = (HMCacheObject) is.readObject();
                if (o.getIdentifier().equals(id)) {
                    return o.object;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        ObjectOutputStream os2 = null;
        ReadWriteLock lock = new ReentrantReadWriteLock();
        lock.writeLock().lock();
        try {
            os2 = new ObjectOutputStream(new FileOutputStream(fileName, true)) {
                protected void writeStreamHeader() throws IOException {
                    reset();
                }
            };
            if (objectsInCache < maxObjectsInCache) {
                os2.writeObject(new HMCacheObject<>(value, key, secondToLive));
                objectsInCache++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lock.writeLock().unlock();
        }


    }

    @Override
    public void cleanup() {
        if (objectsInCache < maxObjectsInCache) return;
        if (overflowCounter > maxObjectsInCache * diskCacheCleanupFactor / 100) {
            resetCacheFile();
        }
    }

    private void resetCacheFile() {

        ObjectOutputStream os1 = null;
        try {
            os1 = new ObjectOutputStream(new FileOutputStream(fileName));
            objectsInCache = 0;
            overflowCounter = 0;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
