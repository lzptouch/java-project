package com.example.rpc.client;

import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;

import java.util.concurrent.CompletableFuture;

/**
 * RPC客户端接口
 */
public interface RpcClient {
    
    /**
     * 初始化客户端
     */
    void init();
    
    /**
     * 发送RPC请求
     * @param request RPC请求对象
     * @return RPC响应对象的CompletableFuture
     */
    CompletableFuture<RpcResponse> sendRequest(RpcRequest request);
    
    /**
     * 关闭客户端
     */
    void close();
    
    /**
     * 设置超时时间
     * @param timeout 超时时间（毫秒）
     */
    void setTimeout(long timeout);
    
    /**
     * 获取超时时间
     */
    long getTimeout();
    
    /**
     * 获取客户端状态
     * @return 是否初始化
     */
    boolean isInitialized();
}