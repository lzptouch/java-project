package com.example.rpc.client.impl;

import com.example.rpc.client.RpcClient;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.registry.RegistryFactory;
import com.example.rpc.registry.ServiceRegistry;
import com.example.rpc.registry.ServiceRegistryInfo;
import com.example.rpc.serializer.Serializer;
import com.example.rpc.serializer.SerializerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于Vertx的RPC客户端实现
 */
@Slf4j
public class VertxRpcClient implements RpcClient {
    
    private Vertx vertx;
    private NetClient netClient;
    private final Serializer serializer;
    private final ServiceRegistry registry;
    
    private long timeout = RpcConstant.DEFAULT_TIMEOUT; // 默认超时时间
    private boolean initialized = false;
    
    // 用于存储请求和对应的CompletableFuture
    private final Map<String, CompletableFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();
    
    public VertxRpcClient() {
        this.serializer = SerializerFactory.getDefaultSerializer();
        this.registry = RegistryFactory.getRegistry();
    }
    
    @Override
    public void init() {
        if (initialized) {
            return;
        }
        
        try {
            this.vertx = Vertx.vertx();
            this.netClient = vertx.createNetClient();
            this.initialized = true;
            log.info("Vertx RPC client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Vertx RPC client", e);
            throw new RuntimeException("Failed to initialize Vertx RPC client", e);
        }
    }
    
    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest request) {
        // 从注册中心获取服务地址
        ServiceRegistryInfo serviceInfo = registry.discover(
                request.getServiceName(), request.getVersion(), request.getGroup());
        
        if (serviceInfo == null) {
            CompletableFuture<RpcResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(
                    new RuntimeException("Service not found: " + request.getServiceName() + 
                            ", version: " + request.getVersion() + 
                            ", group: " + request.getGroup()));
            return failedFuture;
        }
        
        String address = serviceInfo.getAddress();
        return sendRequestToAddress(request, address);
    }
    
    private CompletableFuture<RpcResponse> sendRequestToAddress(RpcRequest request, String address) {
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        String requestId = request.getRequestId();
        
        // 存储请求
        requestMap.put(requestId, future);
        
        // 解析地址
        String[] parts = address.split(":");
        if (parts.length != 2) {
            future.completeExceptionally(new IllegalArgumentException("Invalid address format: " + address));
            requestMap.remove(requestId);
            return future;
        }
        
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        
        try {
            // 连接服务端并发送请求
            netClient.connect(port, host, connect -> {
                if (connect.failed()) {
                    log.error("Failed to connect to server: {}:{}", host, port, connect.cause());
                    future.completeExceptionally(new RuntimeException("Failed to connect to server: " + 
                            connect.cause().getMessage()));
                    requestMap.remove(requestId);
                    return;
                }
                
                NetSocket socket = connect.result();
                
                // 处理响应
                socket.handler(buffer -> {
                    try {
                        handleResponse(buffer, requestId);
                    } catch (Exception e) {
                        log.error("Failed to handle response for request: {}", requestId, e);
                        future.completeExceptionally(e);
                        requestMap.remove(requestId);
                    } finally {
                        socket.close();
                    }
                });
                
                // 处理连接关闭
                socket.closeHandler(v -> {
                    // 连接关闭但没有收到响应，标记为失败
                    CompletableFuture<RpcResponse> pendingFuture = requestMap.remove(requestId);
                    if (pendingFuture != null && !pendingFuture.isDone()) {
                        pendingFuture.completeExceptionally(
                                new RuntimeException("Connection closed before receiving response"));
                    }
                });
                
                // 序列化并发送请求
                try {
                    byte[] requestBytes = serializer.serialize(request);
                    Buffer buffer = Buffer.buffer();
                    // 添加魔术数字
                    buffer.appendByte(RpcConstant.MAGIC_NUMBER);
                    // 添加版本号
                    buffer.appendByte(RpcConstant.VERSION);
                    // 添加消息类型
                    buffer.appendByte(RpcConstant.MESSAGE_TYPE_REQUEST);
                    // 添加请求体长度
                    buffer.appendInt(requestBytes.length);
                    // 添加请求体
                    buffer.appendBytes(requestBytes);
                    
                    socket.write(buffer);
                    
                    // 设置超时处理
                    vertx.setTimer(timeout, timerId -> {
                        CompletableFuture<RpcResponse> timeoutFuture = requestMap.remove(requestId);
                        if (timeoutFuture != null && !timeoutFuture.isDone()) {
                            timeoutFuture.completeExceptionally(
                                    new TimeoutException("Request timeout after " + timeout + "ms"));
                        }
                        socket.close();
                    });
                    
                } catch (Exception e) {
                    log.error("Failed to send request: {}", requestId, e);
                    future.completeExceptionally(e);
                    requestMap.remove(requestId);
                    socket.close();
                }
            });
        } catch (Exception e) {
            log.error("Error sending request: {}", requestId, e);
            future.completeExceptionally(e);
            requestMap.remove(requestId);
        }
        
        return future;
    }
    
    private void handleResponse(Buffer buffer, String requestId) {
        // 检查头部信息
        if (buffer.length() < RpcConstant.HEADER_LENGTH) {
            log.error("Invalid response header length: {}", buffer.length());
            completeWithError(requestId, "Invalid response header");
            return;
        }
        
        // 验证魔术数字
        byte magicNumber = buffer.getByte(0);
        if (magicNumber != RpcConstant.MAGIC_NUMBER) {
            log.error("Invalid magic number: {}", magicNumber);
            completeWithError(requestId, "Invalid magic number");
            return;
        }
        
        // 检查版本号
        byte version = buffer.getByte(1);
        if (version != RpcConstant.VERSION) {
            log.warn("Version mismatch: expected {}, got {}", RpcConstant.VERSION, version);
        }
        
        // 验证消息类型
        byte messageType = buffer.getByte(2);
        if (messageType != RpcConstant.MESSAGE_TYPE_RESPONSE) {
            log.error("Invalid message type: {}", messageType);
            completeWithError(requestId, "Invalid message type");
            return;
        }
        
        // 读取响应体长度
        int bodyLength = buffer.getInt(4);
        
        // 检查完整响应
        if (buffer.length() < RpcConstant.HEADER_LENGTH + bodyLength) {
            log.error("Incomplete response: received {}, expected {}", 
                    buffer.length(), RpcConstant.HEADER_LENGTH + bodyLength);
            completeWithError(requestId, "Incomplete response");
            return;
        }
        
        // 提取响应体
        byte[] responseBytes = buffer.getBytes(RpcConstant.HEADER_LENGTH, 
                RpcConstant.HEADER_LENGTH + bodyLength);
        
        try {
            // 反序列化响应
            RpcResponse response = serializer.deserialize(responseBytes, RpcResponse.class);
            response.setRequestId(requestId);
            
            // 完成请求
            CompletableFuture<RpcResponse> future = requestMap.remove(requestId);
            if (future != null) {
                future.complete(response);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize response for request: {}", requestId, e);
            completeWithError(requestId, "Failed to deserialize response: " + e.getMessage());
        }
    }
    
    private void completeWithError(String requestId, String errorMessage) {
        CompletableFuture<RpcResponse> future = requestMap.remove(requestId);
        if (future != null && !future.isDone()) {
            RpcResponse errorResponse = RpcResponse.builder()
                    .requestId(requestId)
                    .status(RpcConstant.RESPONSE_STATUS_ERROR)
                    .message(errorMessage)
                    .build();
            future.complete(errorResponse);
        }
    }
    
    @Override
    public void close() {
        if (!initialized) {
            return;
        }
        
        try {
            // 取消所有未完成的请求
            for (Map.Entry<String, CompletableFuture<RpcResponse>> entry : requestMap.entrySet()) {
                CompletableFuture<RpcResponse> future = entry.getValue();
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
            requestMap.clear();
            
            // 关闭网络客户端
            if (netClient != null) {
                netClient.close();
            }
            
            // 关闭Vertx实例
            if (vertx != null) {
                vertx.close();
            }
            
            initialized = false;
            log.info("Vertx RPC client closed successfully");
        } catch (Exception e) {
            log.error("Error closing Vertx RPC client", e);
        }
    }
    
    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public long getTimeout() {
        return timeout;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
}