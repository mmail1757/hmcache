package abv;

import java.io.IOException;

/**
 * Created by user on 15/04/16.
 */
public class DiskCacheMock<K, V> implements DiskCache<K, V> {

    @Override
    public V get(K id) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public void cleanup() {

    }
}
