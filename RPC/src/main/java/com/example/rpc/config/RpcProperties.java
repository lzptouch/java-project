package com.example.rpc.config;

import com.example.rpc.constant.RpcConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RPC框架配置属性
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {
    
    /**
     * 服务器端口
     */
    private int serverPort = RpcConstant.DEFAULT_PORT;
    
    /**
     * 服务器主机
     */
    private String serverHost = RpcConstant.DEFAULT_HOST;
    
    /**
     * 注册中心地址
     */
    private String registryAddress = RpcConstant.DEFAULT_REGISTRY_ADDRESS;
    
    /**
     * 注册中心类型
     */
    private String registryType = RpcConstant.REGISTRY_TYPE_ETCD;
    
    /**
     * 默认序列化器
     */
    private String serializer = RpcConstant.DEFAULT_SERIALIZER;
    
    /**
     * 默认负载均衡器
     */
    private String loadBalancer = RpcConstant.DEFAULT_LOAD_BALANCER;
    
    /**
     * 默认重试策略
     */
    private String retryStrategy = RpcConstant.DEFAULT_RETRY_STRATEGY;
    
    /**
     * 默认容错策略
     */
    private String toleranceStrategy = RpcConstant.DEFAULT_TOLERANCE_STRATEGY;
    
    /**
     * 最大重试次数
     */
    private int maxRetryCount = RpcConstant.DEFAULT_MAX_RETRY_COUNT;
    
    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = RpcConstant.DEFAULT_RETRY_INTERVAL;
    
    /**
     * 心跳间隔（毫秒）
     */
    private long heartbeatInterval = RpcConstant.DEFAULT_HEARTBEAT_INTERVAL;
    
    /**
     * 租约时间（毫秒）
     */
    private long leaseTtl = RpcConstant.DEFAULT_LEASE_TTL;
    
    /**
     * 是否启用服务发现
     */
    private boolean enableDiscovery = true;
    
    /**
     * 是否启用服务注册
     */
    private boolean enableRegistry = true;
}