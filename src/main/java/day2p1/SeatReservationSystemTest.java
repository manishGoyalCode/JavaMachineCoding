package day2p1;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SeatReservationSystemTest {

    private SeatReservationSystem system;

    @BeforeEach
    void setup() {
        system = new SeatReservationSystemImpl(); // <-- your implementation
    }

    // ------------------------
    // Happy Path
    // ------------------------

    @Test
    void testHoldAndConfirmWithinTimeout() {
        String holdId = system.holdSeat("A1", "user1");
        assertNotNull(holdId);

        boolean confirmed = system.confirmSeat(holdId);
        assertTrue(confirmed);
    }

    @Test
    void testMultipleSeatsMultipleUsers() {
        String h1 = system.holdSeat("A1", "user1");
        String h2 = system.holdSeat("A2", "user2");

        assertTrue(system.confirmSeat(h1));
        assertTrue(system.confirmSeat(h2));
    }

    // ------------------------
    // Expiry Tests
    // ------------------------

    @Test
    void testConfirmAfterExpiryFails() throws InterruptedException {
        String holdId = system.holdSeat("B1", "user1");
        assertNotNull(holdId);

        Thread.sleep(4000); // wait beyond 3s timeout

        boolean confirmed = system.confirmSeat(holdId);
        assertFalse(confirmed);
    }

    @Test
    void testSeatReleasedAfterExpiry() throws InterruptedException {
        String holdId = system.holdSeat("B2", "user1");
        Thread.sleep(4000);

        // seat should be free again
        String newHold = system.holdSeat("B2", "user2");
        assertNotNull(newHold);
        assertNotEquals(holdId, newHold);
    }

    // ------------------------
    // Validation / Edge Cases
    // ------------------------

    @Test
    void testDoubleHoldSameSeatFails() {
        String h1 = system.holdSeat("C1", "user1");
        assertNotNull(h1);

        String h2 = system.holdSeat("C1", "user2");
        assertNull(h2);
    }

    @Test
    void testConfirmInvalidHoldIdFails() {
        assertFalse(system.confirmSeat("invalid-hold-id"));
    }

    @Test
    void testConfirmAlreadyConfirmedHoldFails() {
        String holdId = system.holdSeat("C2", "user1");
        assertTrue(system.confirmSeat(holdId));

        assertFalse(system.confirmSeat(holdId));
    }

    @Test
    void testOnlySameUserCanConfirm() {
        String holdId = system.holdSeat("D1", "user1");

        // simulate wrong user by system design (implementation should check)
        boolean confirmed = system.confirmSeat(holdId + "-tampered");
        assertFalse(confirmed);
    }

    // ------------------------
    // Concurrency Tests
    // ------------------------

    @Test
    void testConcurrentHoldSameSeatOnlyOneSucceeds() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        Runnable task1 = () -> {
            results.add(system.holdSeat("E1", "user1"));
            latch.countDown();
        };

        Runnable task2 = () -> {
            results.add(system.holdSeat("E1", "user2"));
            latch.countDown();
        };

        executor.submit(task1);
        executor.submit(task2);

        latch.await();
        executor.shutdown();

        long successCount = results.stream().filter(Objects::nonNull).count();
        assertEquals(1, successCount);
    }

    @Test
    void testConcurrentConfirmSameHoldOnlyOneSucceeds() throws InterruptedException {
        String holdId = system.holdSeat("E2", "user1");
        assertNotNull(holdId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);

        Runnable confirmTask = () -> {
            if (system.confirmSeat(holdId)) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        };

        executor.submit(confirmTask);
        executor.submit(confirmTask);

        latch.await();
        executor.shutdown();

        assertEquals(1, successCount.get());
    }

    @Test
    void testHighConcurrencyMultipleSeats() throws InterruptedException {
        int concurrency = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(concurrency);

        Set<String> holds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < concurrency; i++) {
            final int idx = i;
            executor.submit(() -> {
                String hold = system.holdSeat("F" + idx, "user" + idx);
                if (hold != null) {
                    holds.add(hold);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(concurrency, holds.size());
    }
}

