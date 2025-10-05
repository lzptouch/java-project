package com.example.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.exception.RpcException;
import com.example.rpc.model.ServiceRegistryInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 基于Etcd的服务注册中心实现
 */
@Slf4j
public class EtcdRegistry implements ServiceRegistry {
    
    private Client client;
    private KV kvClient;
    private Watch watchClient;
    private Lease leaseClient;
    private ScheduledExecutorService heartbeatExecutor;
    
    /**
     * 本地服务缓存
     */
    private final Map<String, List<ServiceRegistryInfo>> serviceCache = new ConcurrentHashMap<>();
    
    /**
     * 监听器映射
     */
    private final Map<String, List<ServiceChangeListener>> listeners = new ConcurrentHashMap<>();
    
    /**
     * 租约ID映射
     */
    private final Map<String, Long> leaseMap = new ConcurrentHashMap<>();
    
    @Override
    public void init(String address) {
        try {
            client = Client.builder()
                    .endpoints(address)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            kvClient = client.getKVClient();
            watchClient = client.getWatchClient();
            leaseClient = client.getLeaseClient();
            heartbeatExecutor = Executors.newScheduledThreadPool(1, r -> {
                Thread thread = new Thread(r, "rpc-heartbeat");
                thread.setDaemon(true);
                return thread;
            });
            log.info("Etcd registry initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Etcd registry", e);
            throw new RpcException("Failed to initialize Etcd registry", e);
        }
    }
    
    @Override
    public CompletableFuture<Boolean> register(ServiceRegistryInfo registryInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String serviceKey = registryInfo.getServiceKey();
                String registerKey = RpcConstant.SERVICE_REGISTER_PREFIX + serviceKey + "/" + registryInfo.getAddress();
                
                // 创建租约
                long leaseId = leaseClient.grant(RpcConstant.DEFAULT_LEASE_TTL).get().getID();
                leaseMap.put(registerKey, leaseId);
                
                // 序列化服务信息
                byte[] value = serializeServiceInfo(registryInfo);
                
                // 设置key-value并绑定租约
                kvClient.put(
                        ByteSequence.from(registerKey, StandardCharsets.UTF_8),
                        ByteSequence.from(value, StandardCharsets.UTF_8),
                        PutOption.newBuilder().withLeaseId(leaseId).build()
                ).get();
                
                // 启动心跳续约
                startHeartbeat(registerKey, leaseId);
                
                log.info("Service registered: {} at {}", serviceKey, registryInfo.getAddress());
                return true;
            } catch (Exception e) {
                log.error("Failed to register service: {}", registryInfo.getServiceKey(), e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> unregister(ServiceRegistryInfo registryInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String serviceKey = registryInfo.getServiceKey();
                String registerKey = RpcConstant.SERVICE_REGISTER_PREFIX + serviceKey + "/" + registryInfo.getAddress();
                
                // 删除key
                kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
                
                // 撤销租约
                Long leaseId = leaseMap.remove(registerKey);
                if (leaseId != null) {
                    leaseClient.revoke(leaseId);
                }
                
                log.info("Service unregistered: {} from {}", serviceKey, registryInfo.getAddress());
                return true;
            } catch (Exception e) {
                log.error("Failed to unregister service: {}", registryInfo.getServiceKey(), e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<ServiceRegistryInfo>> discover(String serviceName, String group, String version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String serviceKey = String.format("%s:%s:%s", serviceName, group, version);
                String prefix = RpcConstant.SERVICE_REGISTER_PREFIX + serviceKey + "/";
                
                // 尝试从缓存获取
                List<ServiceRegistryInfo> cachedServices = serviceCache.get(serviceKey);
                if (cachedServices != null && !cachedServices.isEmpty()) {
                    return cachedServices;
                }
                
                // 从etcd查询
                List<ServiceRegistryInfo> services = new ArrayList<>();
                List<KeyValue> kvs = kvClient.get(
                        ByteSequence.from(prefix, StandardCharsets.UTF_8),
                        GetOption.newBuilder().withPrefix(ByteSequence.from(prefix, StandardCharsets.UTF_8)).build()
                ).get().getKvs();
                
                for (KeyValue kv : kvs) {
                    ServiceRegistryInfo info = deserializeServiceInfo(kv.getValue().getBytes());
                    if (info != null && info.isHealthy()) {
                        services.add(info);
                    }
                }
                
                // 更新缓存
                serviceCache.put(serviceKey, services);
                
                log.debug("Discovered {} instances for service: {}", services.size(), serviceKey);
                return services;
            } catch (Exception e) {
                log.error("Failed to discover service: {}", serviceName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> renewLease(ServiceRegistryInfo registryInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String serviceKey = registryInfo.getServiceKey();
                String registerKey = RpcConstant.SERVICE_REGISTER_PREFIX + serviceKey + "/" + registryInfo.getAddress();
                
                Long leaseId = leaseMap.get(registerKey);
                if (leaseId != null) {
                    leaseClient.keepAliveOnce(leaseId).get();
                    log.debug("Renewed lease for service: {} at {}", serviceKey, registryInfo.getAddress());
                    return true;
                }
                return false;
            } catch (Exception e) {
                log.error("Failed to renew lease for service: {}", registryInfo.getServiceKey(), e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public void subscribe(String serviceName, String group, String version, ServiceChangeListener listener) {
        try {
            String serviceKey = String.format("%s:%s:%s", serviceName, group, version);
            String prefix = RpcConstant.SERVICE_REGISTER_PREFIX + serviceKey + "/";
            
            // 添加监听器
            listeners.computeIfAbsent(serviceKey, k -> new CopyOnWriteArrayList<>()).add(listener);
            
            // 创建watch
            WatchOption watchOption = WatchOption.newBuilder()
                    .withPrefix(ByteSequence.from(prefix, StandardCharsets.UTF_8))
                    .build();
            
            Watcher watcher = watchClient.watch(ByteSequence.from(prefix, StandardCharsets.UTF_8), watchOption, response -> {
                for (WatchEvent event : response.getEvents()) {
                    handleWatchEvent(serviceKey, event);
                }
            });
            
            log.info("Subscribed to service changes: {}", serviceKey);
        } catch (Exception e) {
            log.error("Failed to subscribe to service: {}", serviceName, e);
            throw new RpcException("Failed to subscribe to service", e);
        }
    }
    
    @Override
    public void unsubscribe(String serviceName, String group, String version, ServiceChangeListener listener) {
        String serviceKey = String.format("%s:%s:%s", serviceName, group, version);
        List<ServiceChangeListener> serviceListeners = listeners.get(serviceKey);
        if (serviceListeners != null) {
            serviceListeners.remove(listener);
            if (serviceListeners.isEmpty()) {
                listeners.remove(serviceKey);
            }
            log.info("Unsubscribed from service changes: {}", serviceKey);
        }
    }
    
    @Override
    public void close() {
        try {
            // 取消所有心跳任务
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdownNow();
            }
            
            // 关闭客户端
            if (client != null) {
                client.close();
            }
            
            log.info("Etcd registry closed");
        } catch (Exception e) {
            log.error("Error closing Etcd registry", e);
        }
    }
    
    /**
     * 启动心跳续约
     */
    private void startHeartbeat(String registerKey, long leaseId) {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                leaseClient.keepAliveOnce(leaseId).get();
                log.debug("Heartbeat sent for lease: {}", leaseId);
            } catch (Exception e) {
                log.error("Heartbeat failed for lease: {}", leaseId, e);
            }
        }, RpcConstant.DEFAULT_HEARTBEAT_INTERVAL / 2, RpcConstant.DEFAULT_HEARTBEAT_INTERVAL / 2, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 处理watch事件
     */
    private void handleWatchEvent(String serviceKey, WatchEvent event) {
        try {
            List<ServiceRegistryInfo> services = discoverServices(serviceKey);
            
            // 更新缓存
            serviceCache.put(serviceKey, services);
            
            // 通知监听器
            List<ServiceChangeListener> serviceListeners = listeners.get(serviceKey);
            if (CollUtil.isNotEmpty(serviceListeners)) {
                for (ServiceChangeListener listener : serviceListeners) {
                    try {
                        listener.onServicesChanged(serviceKey, services);
                    } catch (Exception e) {
                        log.error("Error notifying listener for service: {}", serviceKey, e);
                    }
                }
            }
            
            log.debug("Service changed: {}, instances: {}", serviceKey, services.size());
        } catch (Exception e) {
            log.error("Failed to handle watch event for service: {}", serviceKey, e);
        }
    }
    
    /**
     * 重新发现服务
     */
    private List<ServiceRegistryInfo> discoverServices(String serviceKey) throws Exception {
        String prefix = RpcConstant.SERVICE_REGISTER_PREFIX + serviceKey + "/";
        List<ServiceRegistryInfo> services = new ArrayList<>();
        
        List<KeyValue> kvs = kvClient.get(
                ByteSequence.from(prefix, StandardCharsets.UTF_8),
                GetOption.newBuilder().withPrefix(ByteSequence.from(prefix, StandardCharsets.UTF_8)).build()
        ).get().getKvs();
        
        for (KeyValue kv : kvs) {
            ServiceRegistryInfo info = deserializeServiceInfo(kv.getValue().getBytes());
            if (info != null && info.isHealthy()) {
                services.add(info);
            }
        }
        
        return services;
    }
    
    /**
     * 序列化服务信息
     */
    private byte[] serializeServiceInfo(ServiceRegistryInfo info) {
        // 简化实现，实际应该使用序列化器
        return info.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 反序列化服务信息
     */
    private ServiceRegistryInfo deserializeServiceInfo(byte[] bytes) {
        // 简化实现，实际应该使用序列化器
        try {
            String str = new String(bytes, StandardCharsets.UTF_8);
            // 这里应该根据实际的序列化方式进行反序列化
            // 现在返回一个模拟对象
            ServiceRegistryInfo info = new ServiceRegistryInfo();
            info.setServiceName("unknown");
            info.setHealthy(true);
            return info;
        } catch (Exception e) {
            log.error("Failed to deserialize service info", e);
            return null;
        }
    }
}