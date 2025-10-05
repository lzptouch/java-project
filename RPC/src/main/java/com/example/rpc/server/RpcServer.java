package com.example.rpc.server;

import java.util.Map;

/**
 * RPC服务器接口
 */
public interface RpcServer {
    
    /**
     * 启动服务器
     */
    void start();
    
    /**
     * 关闭服务器
     */
    void shutdown();
    
    /**
     * 注册服务
     * @param serviceInterface 服务接口名
     * @param group 服务分组
     * @param version 服务版本
     * @param weight 服务权重
     */
    void registerService(String serviceInterface, String group, String version, int weight);
    
    /**
     * 注销服务
     * @param serviceInterface 服务接口名
     * @param group 服务分组
     * @param version 服务版本
     */
    void unregisterService(String serviceInterface, String group, String version);
    
    /**
     * 获取服务注册表
     */
    Map<String, Object> getServiceMap();
    
    /**
     * 获取服务器端口
     */
    int getPort();
    
    /**
     * 服务器是否正在运行
     */
    boolean isRunning();
}