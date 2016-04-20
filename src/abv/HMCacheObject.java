package abv;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class HMCacheObject<K, V> implements Cacheable<K>, Serializable {

    private Date dateofExpiration = null;
    private K identifier = null;
    private int secondsToLive;

    public V object = null;

    public HMCacheObject(V obj, K id, int secondsToLive) {

        this.object = obj;
        this.identifier = id;
        this.secondsToLive = secondsToLive;

        if (secondsToLive != 0) {

            refreshExpiration();
        }
    }

    public void refreshExpiration() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(cal.SECOND, secondsToLive);
        dateofExpiration = cal.getTime();
    }

    public boolean isExpired() {

        if (dateofExpiration != null) {

            if (dateofExpiration.before(new Date())) {
                return true;
            }
            else {
                refreshExpiration();
                return false;
            }
        }
        else {
            return false;
        }
    }
    public K getIdentifier() {

        return identifier;
    }
}