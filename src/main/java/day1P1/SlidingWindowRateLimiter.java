package day1P1;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;


public class SlidingWindowRateLimiter implements RateLimiter {
    Map<String, Queue<Instant>> users;
    private final int limit;
    private final int window;

    public SlidingWindowRateLimiter(Integer limit, Integer window) {
        this.limit = limit;
        this.window = window;
        this.users = new ConcurrentHashMap<>();
    }

    public boolean isExpired(Instant timestamp, Instant now) {
        return timestamp.isBefore(now.minusMillis(window));
    }

    @Override
    public boolean allowRequest(String userId) {
        Instant currentTime = Instant.now();
        Queue<Instant> queue = users.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && isExpired(queue.element(), currentTime)) {
                queue.poll();
            }
            if (queue.size() >= limit) {
                return false;
            } else {
                queue.offer(currentTime);
                return true;
            }
        }
    }
}
