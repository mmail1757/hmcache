package abv;

import java.io.*;
import java.util.Properties;

/**
 * Created by user on 13/04/16.
 */
public class TestCache {

    static CacheProcessor<Integer, String> cp;

    public static void main(String[] args) throws InterruptedException , IOException, ClassNotFoundException {

        Properties prop = new Properties();
        InputStream is = null;

        try {

            is = new FileInputStream("config.properties");
            prop.load(is);
        }
        catch (IOException e) {
            System.out.println(e);
        }
        finally {
            is.close();
        }
        boolean isDiskCacheEnabled = Boolean.valueOf(prop.getProperty("enable.disk.cache"));
        cp = new CacheProcessor<>(Integer.valueOf(prop.getProperty("max.size.memory")), Integer.valueOf(prop.getProperty("max.size.disk")),
                isDiskCacheEnabled, Integer.valueOf(prop.getProperty("disk.cache.cleanup.factor")),
                Integer.valueOf(prop.getProperty("memory.objects.lifetime")), Integer.valueOf(prop.getProperty("disk.objects.lifetime")));
        testMemoryCache();
        testCleanupMemoryCache();
        testRefreshExpirationInMemoryCache();
        testMemoryCacheExpiration();
        testDiskCache();
    }

    private static void testMemoryCache() {
        cp.put(1, "object1");
        assert "object1".equals(cp.get(1));
    }

    private static void testRefreshExpirationInMemoryCache() throws InterruptedException {
        cp.put(1, "object1");
        Thread.sleep(1000);
        cp.get(1);
        Thread.sleep(1000);
        cp.get(1);
        Thread.sleep(1000);
        assert "object1".equals(cp.get(1));
    }

    private static void testMemoryCacheExpiration() throws InterruptedException {
        cp.put(1, "object1");
        Thread.sleep(3000);
        assert cp.get(1) == null;

    }

    private static void testCleanupMemoryCache() throws InterruptedException {
        cp.put(1, "object1");
        Thread.sleep(2000);
        assert cp.get(1) == null;
    }

    private static void testDiskCache() throws InterruptedException {

        for (int i = 0; i < 15; i++) {
            cp.put(i, "object" + i);
        }
        assert "object14".equals(cp.get(14));
    }




}
