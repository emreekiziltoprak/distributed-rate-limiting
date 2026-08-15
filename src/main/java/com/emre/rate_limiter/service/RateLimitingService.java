package com.emre.rate_limiter.service;


import com.emre.rate_limiter.core.RateLimiter;
import com.emre.rate_limiter.core.TokenBucketRateLimiter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();


    private static final long BUCKET_CAPACITY = 10;
    private static final long REFILL_RATE = 2;

    /*
    * Controls whether the client has access
    *
    * @param clientId clients IP address, or API key
    * @return If rate isn't exceeded true, else false
    * */
    public boolean allowRequest(String clientId){
        RateLimiter limiter = limiters.computeIfAbsent(clientId, this::createNewBucket);
        return limiter.tryConsume();
    }

    private RateLimiter createNewBucket(String clientId){
    return new TokenBucketRateLimiter(BUCKET_CAPACITY, REFILL_RATE);
    }
}
