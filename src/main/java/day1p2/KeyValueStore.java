package day1p2;

public interface KeyValueStore {
    void put(String key, String value, long ttlMillis);
    String get(String key);
}
