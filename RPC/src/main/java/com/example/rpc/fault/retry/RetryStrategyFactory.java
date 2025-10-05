package com.example.rpc.fault.retry;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 重试策略工厂
 */
@Slf4j
public class RetryStrategyFactory {
    
    /**
     * 获取重试策略实例
     */
    public static RetryStrategy getRetryStrategy(String name) {
        try {
            // 通过SPI加载重试策略实现
            SpiLoader<RetryStrategy> loader = SpiLoader.getInstance(RetryStrategy.class);
            RetryStrategy retryStrategy = loader.getInstance(name);
            log.debug("Loaded retry strategy: {}", name);
            return retryStrategy;
        } catch (Exception e) {
            log.error("Failed to load retry strategy: {}", name, e);
            throw new IllegalArgumentException("Invalid retry strategy type: " + name, e);
        }
    }
    
    /**
     * 获取默认重试策略
     */
    public static RetryStrategy getDefaultRetryStrategy() {
        return getRetryStrategy(RpcConstant.DEFAULT_RETRY_STRATEGY);
    }
}