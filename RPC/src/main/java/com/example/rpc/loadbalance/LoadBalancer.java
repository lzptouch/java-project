package com.example.rpc.loadbalance;

import com.example.rpc.model.ServiceRegistryInfo;

import java.util.List;

/**
 * 负载均衡器接口
 */
public interface LoadBalancer {
    
    /**
     * 从服务列表中选择一个服务实例
     */
    ServiceRegistryInfo select(List<ServiceRegistryInfo> services, String requestId);
    
    /**
     * 获取负载均衡器名称
     */
    String getName();
}