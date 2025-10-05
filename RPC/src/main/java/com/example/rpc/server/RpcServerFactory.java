package com.example.rpc.server;

import com.example.rpc.registry.ServiceRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC服务器工厂
 */
public class RpcServerFactory {
    
    private static final RpcServerFactory instance = new RpcServerFactory();
    
    /**
     * 服务器实例缓存
     */
    private final Map<Integer, RpcServer> serverCache = new ConcurrentHashMap<>();
    
    private RpcServerFactory() {
    }
    
    /**
     * 获取单例实例
     */
    public static RpcServerFactory getInstance() {
        return instance;
    }
    
    /**
     * 创建RPC服务器
     * @param port 端口号
     * @param registry 服务注册中心
     * @param serviceMap 服务映射
     * @return RPC服务器实例
     */
    public RpcServer createServer(int port, ServiceRegistry registry, Map<String, Object> serviceMap) {
        // 检查缓存中是否已有该端口的服务器实例
        return serverCache.computeIfAbsent(port, p -> {
            // 创建默认RPC服务器实现
            RpcServer server = new RpcServerImpl(p, registry, serviceMap);
            return server;
        });
    }
    
    /**
     * 获取指定端口的服务器实例
     * @param port 端口号
     * @return 服务器实例，如果不存在返回null
     */
    public RpcServer getServer(int port) {
        return serverCache.get(port);
    }
    
    /**
     * 关闭指定端口的服务器
     * @param port 端口号
     */
    public void shutdownServer(int port) {
        RpcServer server = serverCache.remove(port);
        if (server != null) {
            server.shutdown();
        }
    }
    
    /**
     * 关闭所有服务器
     */
    public void shutdownAllServers() {
        for (RpcServer server : serverCache.values()) {
            try {
                server.shutdown();
            } catch (Exception e) {
                // 忽略关闭时的异常
            }
        }
        serverCache.clear();
    }
}