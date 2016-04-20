package abv;

/**
 * Created by user on 15/04/16.
 */
public interface DiskCache<K, V> {

    V get(K id);

    void put(K key, V value);

    void cleanup();

}
