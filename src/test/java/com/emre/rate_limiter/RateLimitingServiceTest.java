package com.emre.rate_limiter;

import com.emre.rate_limiter.service.RateLimitingService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitingServiceTest {

    @Test
    void testConcurrentRateLimiting() throws InterruptedException {
        RateLimitingService rateLimitingService = new RateLimitingService();
        String testIp = "192.168.1.1";

        int totalRequests = 20;

        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    boolean isAllowed = rateLimitingService.allowRequest(testIp);
                    if (isAllowed) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        assertEquals(10, successCount.get(), "Successfull requests number should be 10");
        assertEquals(10, failCount.get(), "Failed requests number should be 10");
    }
}