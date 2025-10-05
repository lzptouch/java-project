package com.example.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC客户端工厂
 */
public class RpcClientFactory {
    
    private static final RpcClientFactory instance = new RpcClientFactory();
    
    /**
     * 客户端实例缓存
     */
    private final Map<String, RpcClient> clientCache = new ConcurrentHashMap<>();
    
    private RpcClientFactory() {
    }
    
    /**
     * 获取单例实例
     */
    public static RpcClientFactory getInstance() {
        return instance;
    }
    
    /**
     * 创建RPC客户端
     * @param clientType 客户端类型
     * @return RPC客户端实例
     */
    public RpcClient createClient(String clientType) {
        return clientCache.computeIfAbsent(clientType, type -> {
            RpcClient client;
            switch (type.toLowerCase()) {
                case "vertx":
                    client = new VertxRpcClient();
                    break;
                default:
                    // 默认使用Vertx客户端
                    client = new VertxRpcClient();
                    break;
            }
            client.init();
            return client;
        });
    }
    
    /**
     * 获取默认RPC客户端
     * @return 默认RPC客户端实例
     */
    public RpcClient getDefaultClient() {
        return createClient("vertx");
    }
    
    /**
     * 获取指定类型的客户端实例
     * @param clientType 客户端类型
     * @return 客户端实例，如果不存在返回null
     */
    public RpcClient getClient(String clientType) {
        return clientCache.get(clientType);
    }
    
    /**
     * 关闭指定类型的客户端
     * @param clientType 客户端类型
     */
    public void closeClient(String clientType) {
        RpcClient client = clientCache.remove(clientType);
        if (client != null) {
            client.close();
        }
    }
    
    /**
     * 关闭所有客户端
     */
    public void closeAllClients() {
        for (RpcClient client : clientCache.values()) {
            try {
                client.close();
            } catch (Exception e) {
                // 忽略关闭时的异常
            }
        }
        clientCache.clear();
    }
}