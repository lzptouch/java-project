package com.example.rpc.fault.retry;

import java.util.concurrent.Callable;

/**
 * 重试策略接口
 */
public interface RetryStrategy {
    
    /**
     * 执行带重试的操作
     */
    <T> T doRetry(Callable<T> callable) throws Exception;
    
    /**
     * 获取重试策略名称
     */
    String getName();
}