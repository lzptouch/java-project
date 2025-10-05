package com.example.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务注册信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegistryInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务版本号
     */
    private String version = "1.0";
    
    /**
     * 服务分组
     */
    private String group = "default";
    
    /**
     * 服务实现类名
     */
    private String implClassName;
    
    /**
     * 服务IP地址
     */
    private String host;
    
    /**
     * 服务端口
     */
    private int port;
    
    /**
     * 权重
     */
    private int weight = 100;
    
    /**
     * 健康状态
     */
    private boolean healthy = true;
    
    /**
     * 元数据信息
     */
    private Map<String, String> metadata;
    
    /**
     * 创建时间戳
     */
    private long createTime;
    
    /**
     * 最后心跳时间
     */
    private long lastHeartbeatTime;
    
    /**
     * 获取服务的唯一标识
     */
    public String getServiceKey() {
        return String.format("%s:%s:%s", serviceName, group, version);
    }
    
    /**
     * 获取服务的完整地址
     */
    public String getAddress() {
        return String.format("%s:%d", host, port);
    }
}