package com.example.rpc.loadbalance;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 负载均衡器工厂
 */
@Slf4j
public class LoadBalancerFactory {
    
    /**
     * 获取负载均衡器实例
     */
    public static LoadBalancer getLoadBalancer(String name) {
        try {
            // 通过SPI加载负载均衡器实现
            SpiLoader<LoadBalancer> loader = SpiLoader.getInstance(LoadBalancer.class);
            LoadBalancer loadBalancer = loader.getInstance(name);
            log.debug("Loaded load balancer: {}", name);
            return loadBalancer;
        } catch (Exception e) {
            log.error("Failed to load load balancer: {}", name, e);
            throw new IllegalArgumentException("Invalid load balancer type: " + name, e);
        }
    }
    
    /**
     * 获取默认负载均衡器
     */
    public static LoadBalancer getDefaultLoadBalancer() {
        return getLoadBalancer(RpcConstant.DEFAULT_LOAD_BALANCER);
    }
}