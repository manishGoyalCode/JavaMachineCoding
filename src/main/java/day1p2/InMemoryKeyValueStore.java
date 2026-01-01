package day1p2;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryKeyValueStore implements KeyValueStore {
    static class Value {
        Instant expiry;
        String value;
        Object lock = new Object();

        public Value(String value, Instant expiry) {
            this.expiry = expiry;
            this.value = value;
        }
    }

    ;

    Map<String, Value> map = new ConcurrentHashMap<>();


    @Override
    public String get(String key) {
        Instant now = Instant.now();
        Value value = map.get(key);
        if (value == null) {
            return null;
        }
        synchronized (value.lock) {
            if (value.expiry.isBefore(now)) {
                map.remove(key);
                return null;
            }
            return value.value;
        }
    }

    @Override
    public void put(String key, String value, long ttlMillis) {
        Instant expiry = Instant.now().plusMillis(ttlMillis);
        map.compute(key,(k,existing)->{
            if (existing == null) {
                return new Value(value, expiry);
            }
            synchronized (existing.lock) {
                existing.expiry = expiry;
                existing.value = value;
                return existing;
            }
        });
    }
}
