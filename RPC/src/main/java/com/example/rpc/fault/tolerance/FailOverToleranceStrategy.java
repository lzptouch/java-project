package com.example.rpc.fault.tolerance;

import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 故障转移容错策略实现
 */
@Slf4j
public class FailOverToleranceStrategy implements ToleranceStrategy {
    
    @Override
    public RpcResponse doTolerance(RpcRequest request, Throwable e) {
        log.error("FailOver strategy triggered for request: {}.{} - {}", 
                request.getServiceName(), request.getMethodName(), e.getMessage());
        
        // 在实际实现中，这里应该尝试切换到其他服务实例
        // 但由于我们没有服务实例列表，这里返回失败响应
        return RpcResponse.failure(request.getRequestId(), 
                "Service call failed after failover: " + e.getMessage());
    }
    
    @Override
    public String getName() {
        return "failOver";
    }
}