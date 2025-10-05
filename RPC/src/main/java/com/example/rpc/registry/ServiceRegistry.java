package com.example.rpc.registry;

import com.example.rpc.model.ServiceRegistryInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 服务注册中心接口
 */
public interface ServiceRegistry {
    
    /**
     * 初始化注册中心
     */
    void init(String address);
    
    /**
     * 注册服务
     */
    CompletableFuture<Boolean> register(ServiceRegistryInfo registryInfo);
    
    /**
     * 注销服务
     */
    CompletableFuture<Boolean> unregister(ServiceRegistryInfo registryInfo);
    
    /**
     * 发现服务
     */
    CompletableFuture<List<ServiceRegistryInfo>> discover(String serviceName, String group, String version);
    
    /**
     * 更新服务租约（心跳）
     */
    CompletableFuture<Boolean> renewLease(ServiceRegistryInfo registryInfo);
    
    /**
     * 订阅服务变更
     */
    void subscribe(String serviceName, String group, String version, ServiceChangeListener listener);
    
    /**
     * 取消订阅
     */
    void unsubscribe(String serviceName, String group, String version, ServiceChangeListener listener);
    
    /**
     * 关闭注册中心连接
     */
    void close();
    
    /**
     * 服务变更监听器
     */
    interface ServiceChangeListener {
        void onServicesChanged(String serviceKey, List<ServiceRegistryInfo> services);
    }
}