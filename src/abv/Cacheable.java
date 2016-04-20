package abv;

public interface Cacheable<K> {
    boolean isExpired();
    K getIdentifier();
    void refreshExpiration();

}