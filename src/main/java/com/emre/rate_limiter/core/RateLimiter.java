package com.emre.rate_limiter.core;

public interface RateLimiter {
    /*
    *  Controls whether a request will be allowed or not
    *  @return If the bucket has enough tokens, true;
    *  if limit is exceeded then return false
    * */
    boolean tryConsume();
}
