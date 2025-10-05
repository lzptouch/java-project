package com.example.rpc.fault.tolerance;

import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 直接失败容错策略实现
 */
@Slf4j
public class FailFastToleranceStrategy implements ToleranceStrategy {
    
    @Override
    public RpcResponse doTolerance(RpcRequest request, Throwable e) {
        log.error("FailFast strategy triggered for request: {}.{} - {}", 
                request.getServiceName(), request.getMethodName(), e.getMessage());
        
        // 直接返回失败响应
        return RpcResponse.failure(request.getRequestId(), 
                "Service call failed: " + e.getMessage());
    }
    
    @Override
    public String getName() {
        return "failFast";
    }
}