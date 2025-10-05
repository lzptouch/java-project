package com.example.rpc.client;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.serializer.SerializerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于Vertx的RPC客户端实现
 */
@Slf4j
public class VertxRpcClient implements RpcClient {
    
    private final Vertx vertx;
    private final NetClient netClient;
    private final Map<String, CompletableFuture<RpcResponse>> requestMap;
    private long timeout;
    private boolean initialized = false;
    
    public VertxRpcClient() {
        this.vertx = Vertx.vertx();
        this.netClient = vertx.createNetClient();
        this.requestMap = new ConcurrentHashMap<>();
        this.timeout = RpcConstant.DEFAULT_TIMEOUT;
    }
    
    @Override
    public void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        log.info("RPC client initialized");
    }
    
    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest request) {
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        
        // 生成请求ID（如果没有的话）
        if (request.getRequestId() == null) {
            request.setRequestId(UUID.randomUUID().toString());
        }
        
        String requestId = request.getRequestId();
        
        // 存储请求的future，以便响应时回调
        requestMap.put(requestId, future);
        
        // 设置超时处理
        vertx.setTimer(timeout, id -> {
            CompletableFuture<RpcResponse> timeoutFuture = requestMap.remove(requestId);
            if (timeoutFuture != null) {
                timeoutFuture.completeExceptionally(new TimeoutException("Request timeout after " + timeout + "ms"));
            }
        });
        
        String host = request.getHost();
        int port = request.getPort();
        
        try {
            // 连接服务器
            netClient.connect(port, host, connectResult -> {
                if (connectResult.succeeded()) {
                    NetSocket socket = connectResult.result();
                    
                    // 处理响应
                    socket.handler(buffer -> {
                        try {
                            // 解析响应
                            RpcResponse response = decodeResponse(buffer);
                            
                            // 获取并移除对应的future
                            CompletableFuture<RpcResponse> responseFuture = requestMap.remove(response.getRequestId());
                            if (responseFuture != null) {
                                responseFuture.complete(response);
                            }
                        } catch (Exception e) {
                            log.error("Error decoding response", e);
                            CompletableFuture<RpcResponse> errorFuture = requestMap.remove(requestId);
                            if (errorFuture != null) {
                                errorFuture.completeExceptionally(e);
                            }
                        } finally {
                            // 关闭连接
                            socket.close();
                        }
                    });
                    
                    socket.exceptionHandler(ex -> {
                        log.error("Socket error", ex);
                        CompletableFuture<RpcResponse> errorFuture = requestMap.remove(requestId);
                        if (errorFuture != null) {
                            errorFuture.completeExceptionally(ex);
                        }
                        socket.close();
                    });
                    
                    try {
                        // 发送请求
                        Buffer buffer = encodeRequest(request);
                        socket.write(buffer);
                    } catch (Exception e) {
                        log.error("Error encoding request", e);
                        CompletableFuture<RpcResponse> errorFuture = requestMap.remove(requestId);
                        if (errorFuture != null) {
                            errorFuture.completeExceptionally(e);
                        }
                        socket.close();
                    }
                } else {
                    // 连接失败
                    log.error("Failed to connect to server: {}:{}", host, port, connectResult.cause());
                    CompletableFuture<RpcResponse> errorFuture = requestMap.remove(requestId);
                    if (errorFuture != null) {
                        errorFuture.completeExceptionally(connectResult.cause());
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error sending request", e);
            CompletableFuture<RpcResponse> errorFuture = requestMap.remove(requestId);
            if (errorFuture != null) {
                errorFuture.completeExceptionally(e);
            }
        }
        
        return future;
    }
    
    private Buffer encodeRequest(RpcRequest request) throws Exception {
        // 序列化请求对象
        byte[] data = SerializerFactory.getSerializer(RpcConstant.DEFAULT_SERIALIZER).serialize(request);
        
        // 构建协议头
        Buffer buffer = Buffer.buffer();
        buffer.appendInt(RpcConstant.MAGIC_NUMBER);      // 魔数
        buffer.appendByte(RpcConstant.VERSION);          // 版本号
        buffer.appendByte(RpcConstant.MESSAGE_TYPE_REQUEST); // 消息类型
        buffer.appendByte((byte) RpcConstant.SERIALIZER_JSON.charAt(0)); // 序列化类型
        buffer.appendInt(data.length);                   // 数据长度
        buffer.appendBytes(data);                        // 请求数据
        
        return buffer;
    }
    
    private RpcResponse decodeResponse(Buffer buffer) throws Exception {
        // 读取协议头
        int magicNumber = buffer.getInt(0);
        if (magicNumber != RpcConstant.MAGIC_NUMBER) {
            throw new IllegalArgumentException("Invalid protocol header");
        }
        
        byte version = buffer.getByte(4);
        byte messageType = buffer.getByte(5);
        
        if (messageType != RpcConstant.MESSAGE_TYPE_RESPONSE) {
            throw new IllegalArgumentException("Invalid message type");
        }
        
        // 读取序列化类型
        byte serializerType = buffer.getByte(6);
        
        // 读取数据长度
        int dataLength = buffer.getInt(8);
        
        // 读取响应数据
        byte[] data = buffer.getBytes(12, 12 + dataLength);
        
        // 反序列化响应对象
        return SerializerFactory.getSerializer(serializerType).deserialize(data, RpcResponse.class);
    }
    
    @Override
    public void close() {
        if (netClient != null) {
            netClient.close();
        }
        if (vertx != null) {
            vertx.close();
        }
        
        // 取消所有未完成的请求
        for (Map.Entry<String, CompletableFuture<RpcResponse>> entry : requestMap.entrySet()) {
            if (!entry.getValue().isDone()) {
                entry.getValue().cancel(true);
            }
        }
        requestMap.clear();
        
        initialized = false;
        log.info("RPC client closed");
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