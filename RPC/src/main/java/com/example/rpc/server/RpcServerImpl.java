package com.example.rpc.server;

import com.example.rpc.registry.ServiceRegistry;
import com.example.rpc.constant.RpcConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认RPC服务器实现（包装VertxRpcServer）
 */
@Slf4j
public class RpcServerImpl implements RpcServer {
    
    private final VertxRpcServer delegate;
    private final ServiceRegistry registry;
    
    public RpcServerImpl(int port, ServiceRegistry registry, Map<String, Object> serviceMap) {
        this.registry = registry;
        this.delegate = new VertxRpcServer(port, registry, serviceMap != null ? serviceMap : new ConcurrentHashMap<>());
    }
    
    @Override
    public void start() {
        try {
            delegate.start();
            log.info("RPC server started on port: {}", delegate.getPort());
        } catch (Exception e) {
            log.error("Failed to start RPC server", e);
            throw new RuntimeException("Failed to start RPC server", e);
        }
    }
    
    @Override
    public void shutdown() {
        try {
            delegate.shutdown();
            log.info("RPC server shut down");
        } catch (Exception e) {
            log.error("Error during server shutdown", e);
        }
    }
    
    @Override
    public void registerService(String serviceInterface, String group, String version, int weight) {
        // 构建服务键
        String serviceKey = serviceInterface + ":" + group + ":" + version;
        log.info("Registering service: {} with weight: {}", serviceKey, weight);
        
        // 调用委托对象的方法
        delegate.registerService(serviceInterface, group, version, weight);
        
        // 通过注册中心注册服务
        if (registry != null) {
            try {
                registry.register(serviceInterface, group, version, buildServiceUrl(serviceInterface, group, version), weight);
            } catch (Exception e) {
                log.error("Failed to register service to registry: {}", serviceKey, e);
            }
        }
    }
    
    @Override
    public void unregisterService(String serviceInterface, String group, String version) {
        // 构建服务键
        String serviceKey = serviceInterface + ":" + group + ":" + version;
        log.info("Unregistering service: {}", serviceKey);
        
        // 调用委托对象的方法
        delegate.unregisterService(serviceInterface, group, version);
        
        // 通过注册中心注销服务
        if (registry != null) {
            try {
                registry.unregister(serviceInterface, group, version, buildServiceUrl(serviceInterface, group, version));
            } catch (Exception e) {
                log.error("Failed to unregister service from registry: {}", serviceKey, e);
            }
        }
    }
    
    @Override
    public Map<String, Object> getServiceMap() {
        return delegate.getServiceMap();
    }
    
    @Override
    public int getPort() {
        return delegate.getPort();
    }
    
    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }
    
    /**
     * 构建服务URL
     */
    private String buildServiceUrl(String serviceInterface, String group, String version) {
        return RpcConstant.DEFAULT_HOST + ":" + delegate.getPort();
    }
    
    /**
     * 获取服务注册中心
     */
    public ServiceRegistry getRegistry() {
        return registry;
    }
}