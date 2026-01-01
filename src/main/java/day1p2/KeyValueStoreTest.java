package day1p2;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KeyValueStore interface
 */
public class KeyValueStoreTest {


    private final KeyValueStore store = new InMemoryKeyValueStore();

    // -------------------- HAPPY PATH --------------------

    @Test
    void shouldStoreAndRetrieveValue() {
        store.put("key1", "value1", 1000);
        assertEquals("value1", store.get("key1"));
    }

    // -------------------- TTL EXPIRY --------------------

    @Test
    void shouldExpireValueAfterTTL() throws InterruptedException {
        store.put("key2", "value2", 100);
        Thread.sleep(150);
        assertNull(store.get("key2"));
    }

    @Test
    void shouldReturnNullForExpiredKeyImmediately() {
        store.put("key3", "value3", 0);
        assertNull(store.get("key3"));
    }

    // -------------------- EDGE CASES --------------------

    @Test
    void shouldReturnNullForNonExistingKey() {
        assertNull(store.get("missing-key"));
    }

    @Test
    void shouldOverwriteExistingKeyWithNewValue() {
        store.put("key4", "old", 1000);
        store.put("key4", "new", 1000);
        assertEquals("new", store.get("key4"));
    }

    @Test
    void overwriteShouldResetTTL() throws InterruptedException {
        store.put("key5", "value", 100);
        Thread.sleep(80);

        // overwrite with fresh TTL
        store.put("key5", "value", 200);
        Thread.sleep(150);

        assertEquals("value", store.get("key5"));
    }

    @Test
    void differentKeysShouldBeIndependent() {
        store.put("key6", "value6", 1000);
        store.put("key7", "value7", 1000);

        assertEquals("value6", store.get("key6"));
        assertEquals("value7", store.get("key7"));
    }

    // -------------------- BOUNDARY CONDITIONS --------------------

    @Test
    void valueShouldBeAccessibleJustBeforeExpiry() throws InterruptedException {
        store.put("key8", "value8", 200);
        Thread.sleep(150);
        assertEquals("value8", store.get("key8"));
    }

    @Test
    void valueShouldExpireJustAfterTTLBoundary() throws InterruptedException {
        store.put("key9", "value9", 100);
        Thread.sleep(130);
        assertNull(store.get("key9"));
    }
}
