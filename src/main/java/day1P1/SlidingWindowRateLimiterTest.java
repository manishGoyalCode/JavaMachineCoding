package day1P1;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SlidingWindowRateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimit() {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(3, 1000);

        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
    }

    @Test
    void shouldRejectWhenLimitExceeded() {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
        assertFalse(limiter.allowRequest("user1"));
    }

    @Test
    void shouldAllowAfterWindowExpires() throws InterruptedException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(1, 200);

        assertTrue(limiter.allowRequest("user1"));
        assertFalse(limiter.allowRequest("user1"));

        // wait for window to expire
        Thread.sleep(250);

        assertTrue(limiter.allowRequest("user1"));
    }

    @Test
    void differentUsersShouldHaveIndependentLimits() {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(1, 1000);

        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user2")); // should not block
    }

    @Test
    void oldTimestampsShouldBeCleanedUp() throws InterruptedException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(2, 300);

        assertTrue(limiter.allowRequest("user1"));
        Thread.sleep(200);
        assertTrue(limiter.allowRequest("user1"));

        // first request should now be expired
        Thread.sleep(200);

        assertTrue(limiter.allowRequest("user1"));
    }

    @Test
    void concurrentRequestsShouldRespectLimit() throws InterruptedException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(5, 1000);

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger allowedCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                if (limiter.allowRequest("user1")) {
                    allowedCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        assertEquals(5, allowedCount.get());
    }
}
