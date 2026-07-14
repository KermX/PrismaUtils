package me.kermx.prismaUtils.managers.chat;

public final class TokenBucket {
    private final int capacity;
    private final double refillPerSecond;

    private double tokens;
    private long lastMs;

    public TokenBucket(int capacity, double refillPerSecond, long nowMs) {
        this.capacity = Math.max(1, capacity);
        this.refillPerSecond = Math.max(0.0, refillPerSecond);
        this.tokens = this.capacity;
        this.lastMs = nowMs;
    }

    public boolean tryConsume(double amount, long nowMs) {
        refill(nowMs);
        if (tokens >= amount) {
            tokens -= amount;
            return true;
        }
        return false;
    }

    private void refill(long nowMs) {
        long dt = Math.max(0L, nowMs - lastMs);
        lastMs = nowMs;
        tokens = Math.min(capacity, tokens + (dt / 1000.0) * refillPerSecond);
    }
}
