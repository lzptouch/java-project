package com.example.rpc.fault.tolerance;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 容错策略工厂
 */
@Slf4j
public class ToleranceStrategyFactory {
    
    /**
     * 获取容错策略实例
     */
    public static ToleranceStrategy getToleranceStrategy(String name) {
        try {
            // 通过SPI加载容错策略实现
            SpiLoader<ToleranceStrategy> loader = SpiLoader.getInstance(ToleranceStrategy.class);
            ToleranceStrategy toleranceStrategy = loader.getInstance(name);
            log.debug("Loaded tolerance strategy: {}", name);
            return toleranceStrategy;
        } catch (Exception e) {
            log.error("Failed to load tolerance strategy: {}", name, e);
            throw new IllegalArgumentException("Invalid tolerance strategy type: " + name, e);
        }
    }
    
    /**
     * 获取默认容错策略
     */
    public static ToleranceStrategy getDefaultToleranceStrategy() {
        return getToleranceStrategy(RpcConstant.DEFAULT_TOLERANCE_STRATEGY);
    }
}