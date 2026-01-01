package day1P1;

public interface RateLimiter {
    boolean allowRequest(String userId);
}
