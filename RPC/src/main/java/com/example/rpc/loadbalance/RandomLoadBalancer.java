package com.example.rpc.loadbalance;

import com.example.rpc.model.ServiceRegistryInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡器实现
 */
@Slf4j
public class RandomLoadBalancer implements LoadBalancer {
    
    private final Random random = new Random();
    
    @Override
    public ServiceRegistryInfo select(List<ServiceRegistryInfo> services, String requestId) {
        if (services == null || services.isEmpty()) {
            return null;
        }
        
        // 如果只有一个服务实例，直接返回
        if (services.size() == 1) {
            return services.get(0);
        }
        
        // 随机选择
        int index = random.nextInt(services.size());
        ServiceRegistryInfo selected = services.get(index);
        
        log.debug("Random selected service: {} at {}", selected.getServiceName(), selected.getAddress());
        return selected;
    }
    
    @Override
    public String getName() {
        return "random";
    }
}