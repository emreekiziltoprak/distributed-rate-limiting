package com.emre.rate_limiter.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketRateLimiter implements RateLimiter {

    private final long maxCap;
    private final long refillRatePerSecond;

    private final AtomicLong currentTokens;
    private volatile long lastRefillTime;

    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketRateLimiter(long maxCap, long refillRatePerSecond) {
        this.maxCap = maxCap;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens = new AtomicLong(maxCap);
        this.lastRefillTime = System.currentTimeMillis();
    }

    @Override
    public boolean tryConsume() {
        refill();
        long current = currentTokens.get();

        while(current > 0){
            //if a tread didn't consume then take the token
            if(currentTokens.compareAndSet(current, current-1)){
                return true;
            }
            //if it is consumed then update current
            current = currentTokens.get();
        }
        //so current is zero, no token
        return false;

    }

    public void refill() {
        long now = System.currentTimeMillis();
        {
            if (now <= lastRefillTime) {
                return;
            } else if (lock.tryLock()) {
                try {
                    if (now > lastRefillTime) {
                        //time is elapsed since the filling bucket, refill
                        long elapsedTimeMillis =
                                now - lastRefillTime;
                        long newTokensToFill = (elapsedTimeMillis * refillRatePerSecond) / 1000;

                        if (newTokensToFill > 0) {
                            Math.min(maxCap, currentTokens.get() + newTokensToFill);
                            currentTokens.set(newTokensToFill);
                            lastRefillTime = now;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Err: " + e.getMessage());

                } finally {
                    lock.unlock();
                }
            }
        }
    }

}
