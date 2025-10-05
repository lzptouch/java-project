package com.example.rpc.fault.retry;

import com.example.rpc.constant.RpcConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 指数退避重试策略实现
 */
@Slf4j
public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    
    private final int maxRetryCount;
    private final long initialIntervalMs;
    private final double multiplier;
    private final long maxIntervalMs;
    
    public ExponentialBackoffRetryStrategy() {
        this(RpcConstant.DEFAULT_MAX_RETRY_COUNT, RpcConstant.DEFAULT_RETRY_INTERVAL, 2.0, 30000);
    }
    
    public ExponentialBackoffRetryStrategy(int maxRetryCount, long initialIntervalMs, double multiplier, long maxIntervalMs) {
        this.maxRetryCount = maxRetryCount;
        this.initialIntervalMs = initialIntervalMs;
        this.multiplier = multiplier;
        this.maxIntervalMs = maxIntervalMs;
    }
    
    @Override
    public <T> T doRetry(Callable<T> callable) throws Exception {
        Exception lastException = null;
        
        for (int i = 0; i <= maxRetryCount; i++) {
            try {
                if (i > 0) {
                    log.info("Retrying call, attempt {}/{}", i, maxRetryCount);
                    // 计算指数退避时间
                    long sleepTime = calculateSleepTime(i);
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                }
                
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                long nextSleepTime = calculateSleepTime(i + 1);
                log.warn("Call failed, will retry after {}ms: {}", nextSleepTime, e.getMessage());
            }
        }
        
        log.error("Max retries exceeded: {}", maxRetryCount);
        throw lastException;
    }
    
    /**
     * 计算退避时间
     */
    private long calculateSleepTime(int attempt) {
        long sleepTime = (long) (initialIntervalMs * Math.pow(multiplier, attempt - 1));
        return Math.min(sleepTime, maxIntervalMs);
    }
    
    @Override
    public String getName() {
        return "exponentialBackoff";
    }
}