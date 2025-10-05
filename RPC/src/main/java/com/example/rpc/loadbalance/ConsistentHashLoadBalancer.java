package com.example.rpc.loadbalance;

import com.example.rpc.model.ServiceRegistryInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性Hash负载均衡器实现
 */
@Slf4j
public class ConsistentHashLoadBalancer implements LoadBalancer {
    
    private final int virtualNodes = 100; // 每个真实节点对应的虚拟节点数量
    private final ConcurrentHashMap<String, ConsistentHashRing> ringMap = new ConcurrentHashMap<>();
    
    @Override
    public ServiceRegistryInfo select(List<ServiceRegistryInfo> services, String requestId) {
        if (services == null || services.isEmpty()) {
            return null;
        }
        
        // 如果只有一个服务实例，直接返回
        if (services.size() == 1) {
            return services.get(0);
        }
        
        // 生成服务列表的唯一标识
        String serviceKey = generateServiceKey(services);
        
        // 获取或创建一致性哈希环
        ConsistentHashRing ring = ringMap.computeIfAbsent(serviceKey, k -> new ConsistentHashRing(services));
        
        // 使用请求ID作为哈希键
        ServiceRegistryInfo selected = ring.get(requestId);
        
        log.debug("ConsistentHash selected service: {} at {}", selected.getServiceName(), selected.getAddress());
        return selected;
    }
    
    @Override
    public String getName() {
        return "consistentHash";
    }
    
    /**
     * 生成服务列表的唯一标识
     */
    private String generateServiceKey(List<ServiceRegistryInfo> services) {
        StringBuilder sb = new StringBuilder();
        for (ServiceRegistryInfo service : services) {
            sb.append(service.getAddress()).append(":");
        }
        return sb.toString();
    }
    
    /**
     * 一致性哈希环实现
     */
    private class ConsistentHashRing {
        private final TreeMap<Integer, ServiceRegistryInfo> ring = new TreeMap<>();
        
        public ConsistentHashRing(List<ServiceRegistryInfo> services) {
            for (ServiceRegistryInfo service : services) {
                addNode(service);
            }
        }
        
        private void addNode(ServiceRegistryInfo service) {
            String address = service.getAddress();
            for (int i = 0; i < virtualNodes; i++) {
                String virtualNode = address + ":" + i;
                int hash = getHash(virtualNode);
                ring.put(hash, service);
            }
        }
        
        public ServiceRegistryInfo get(String key) {
            if (ring.isEmpty()) {
                return null;
            }
            
            int hash = getHash(key);
            
            // 获取大于等于hash的最小键
            SortedMap<Integer, ServiceRegistryInfo> tailMap = ring.tailMap(hash);
            if (tailMap.isEmpty()) {
                // 如果没有找到，返回环的第一个节点
                return ring.firstEntry().getValue();
            }
            return tailMap.firstEntry().getValue();
        }
        
        private int getHash(String key) {
            // 使用简单的哈希算法，实际应用中可以使用更复杂的算法
            return key.hashCode();
        }
    }
}