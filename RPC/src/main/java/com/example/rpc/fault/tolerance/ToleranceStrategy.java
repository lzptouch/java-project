package com.example.rpc.fault.tolerance;

import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;

/**
 * 容错策略接口
 */
public interface ToleranceStrategy {
    
    /**
     * 执行容错处理
     */
    RpcResponse doTolerance(RpcRequest request, Throwable e);
    
    /**
     * 获取容错策略名称
     */
    String getName();
}