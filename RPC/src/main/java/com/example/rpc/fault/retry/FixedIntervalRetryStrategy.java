package com.example.rpc.fault.retry;

import com.example.rpc.constant.RpcConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定间隔重试策略实现
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {
    
    private final int maxRetryCount;
    private final long retryIntervalMs;
    
    public FixedIntervalRetryStrategy() {
        this(RpcConstant.DEFAULT_MAX_RETRY_COUNT, RpcConstant.DEFAULT_RETRY_INTERVAL);
    }
    
    public FixedIntervalRetryStrategy(int maxRetryCount, long retryIntervalMs) {
        this.maxRetryCount = maxRetryCount;
        this.retryIntervalMs = retryIntervalMs;
    }
    
    @Override
    public <T> T doRetry(Callable<T> callable) throws Exception {
        Exception lastException = null;
        
        for (int i = 0; i <= maxRetryCount; i++) {
            try {
                if (i > 0) {
                    log.info("Retrying call, attempt {}/{}", i, maxRetryCount);
                    // 等待固定间隔
                    TimeUnit.MILLISECONDS.sleep(retryIntervalMs);
                }
                
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                log.warn("Call failed, will retry after {}ms: {}", retryIntervalMs, e.getMessage());
            }
        }
        
        log.error("Max retries exceeded: {}", maxRetryCount);
        throw lastException;
    }
    
    @Override
    public String getName() {
        return "fixedInterval";
    }
}