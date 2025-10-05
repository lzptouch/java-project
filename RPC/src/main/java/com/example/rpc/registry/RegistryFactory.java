package com.example.rpc.registry;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册中心工厂
 */
@Slf4j
public class RegistryFactory {
    
    private static final ServiceRegistry DEFAULT_REGISTRY;
    
    static {
        // 初始化默认注册中心
        DEFAULT_REGISTRY = createRegistry(RpcConstant.REGISTRY_TYPE_ETCD, RpcConstant.DEFAULT_REGISTRY_ADDRESS);
    }
    
    /**
     * 创建注册中心实例
     */
    public static ServiceRegistry createRegistry(String type, String address) {
        try {
            // 通过SPI加载注册中心实现
            SpiLoader<ServiceRegistry> loader = SpiLoader.getInstance(ServiceRegistry.class);
            ServiceRegistry registry = loader.getInstance(type);
            
            // 初始化注册中心
            registry.init(address);
            log.info("Created registry: {} with address: {}", type, address);
            return registry;
        } catch (Exception e) {
            log.error("Failed to create registry: {}", type, e);
            throw new IllegalArgumentException("Invalid registry type: " + type, e);
        }
    }
    
    /**
     * 获取默认注册中心实例
     */
    public static ServiceRegistry getDefaultRegistry() {
        return DEFAULT_REGISTRY;
    }
    
    /**
     * 获取注册中心实例（与getDefaultRegistry方法保持一致）
     */
    public static ServiceRegistry getRegistry() {
        return DEFAULT_REGISTRY;
    }
}