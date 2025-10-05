package com.example.rpc.server;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.registry.ServiceRegistry;
import com.example.rpc.serializer.SerializerFactory;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于Vertx的RPC服务器实现
 */
@Slf4j
public class VertxRpcServer implements RpcServer {
    
    private final int port;
    private final ServiceRegistry registry;
    private final Map<String, Object> serviceMap;
    private final Vertx vertx;
    private NetServer server;
    private boolean running = false;
    private final ExecutorService workerPool;
    
    public VertxRpcServer(int port, ServiceRegistry registry, Map<String, Object> serviceMap) {
        this.port = port;
        this.registry = registry;
        this.serviceMap = serviceMap != null ? serviceMap : new ConcurrentHashMap<>();
        this.vertx = Vertx.vertx();
        this.workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }
    
    @Override
    public void start() {
        if (running) {
            log.warn("Server is already running on port: {}", port);
            return;
        }
        
        try {
            // 创建TCP服务器
            server = vertx.createNetServer();
            
            // 处理连接请求
            server.connectHandler(this::handleConnection);
            
            // 启动服务器
            server.listen(port, result -> {
                if (result.succeeded()) {
                    running = true;
                    log.info("RPC server started successfully on port: {}", port);
                } else {
                    log.error("Failed to start RPC server on port: {}", port, result.cause());
                }
            });
            
        } catch (Exception e) {
            log.error("Failed to start RPC server", e);
            throw new RuntimeException("Failed to start RPC server", e);
        }
    }
    
    private void handleConnection(NetSocket socket) {
        socket.handler(buffer -> {
            workerPool.execute(() -> {
                try {
                    // 解析请求
                    RpcRequest request = decodeRequest(buffer);
                    
                    // 处理请求
                    RpcResponse<?> response = handleRequest(request);
                    
                    // 发送响应
                    Buffer responseBuffer = encodeResponse(response);
                    socket.write(responseBuffer);
                } catch (Exception e) {
                    log.error("Error handling request", e);
                    // 发送错误响应
                    RpcResponse<?> errorResponse = RpcResponse.builder()
                            .status(RpcConstant.RESPONSE_STATUS_ERROR)
                            .message("Internal server error: " + e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build();
                    try {
                        socket.write(encodeResponse(errorResponse));
                    } catch (Exception ex) {
                        log.error("Failed to send error response", ex);
                    }
                }
            });
        });
        
        socket.exceptionHandler(ex -> {
            log.error("Connection error", ex);
        });
        
        socket.closeHandler(v -> {
            log.debug("Connection closed");
        });
    }
    
    private RpcRequest decodeRequest(Buffer buffer) throws Exception {
        // 读取协议头
        int magicNumber = buffer.getInt(0);
        if (magicNumber != RpcConstant.MAGIC_NUMBER) {
            throw new IllegalArgumentException("Invalid protocol header");
        }
        
        byte version = buffer.getByte(4);
        byte messageType = buffer.getByte(5);
        
        if (messageType != RpcConstant.MESSAGE_TYPE_REQUEST) {
            throw new IllegalArgumentException("Invalid message type");
        }
        
        // 读取序列化类型
        byte serializerType = buffer.getByte(6);
        
        // 读取数据长度
        int dataLength = buffer.getInt(8);
        
        // 读取请求数据
        byte[] data = buffer.getBytes(12, 12 + dataLength);
        
        // 反序列化请求对象
        return SerializerFactory.getSerializer(serializerType).deserialize(data, RpcRequest.class);
    }
    
    private RpcResponse<?> handleRequest(RpcRequest request) {
        try {
            // 构建服务键 - 适配serviceName属性
            String serviceKey = request.getServiceName() + ":" + request.getGroup() + ":" + request.getVersion();
            
            // 获取服务实例
            Object service = serviceMap.get(serviceKey);
            if (service == null) {
                log.error("Service not found: {}", serviceKey);
                return RpcResponse.builder()
                        .status(RpcConstant.RESPONSE_STATUS_ERROR)
                        .message("Service not found: " + serviceKey)
                        .requestId(request.getRequestId())
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
            
            // 反射调用方法
            Class<?> serviceClass = service.getClass();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Method method = serviceClass.getMethod(request.getMethodName(), parameterTypes);
            
            Object result = method.invoke(service, request.getParameters());
            
            // 返回成功响应
            return RpcResponse.builder()
                    .status(RpcConstant.RESPONSE_STATUS_SUCCESS)
                    .data(result)
                    .requestId(request.getRequestId())
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error executing method", e);
            
            // 处理方法不存在的情况
            if (e.getCause() instanceof NoSuchMethodException) {
                return RpcResponse.builder()
                        .status(RpcConstant.RESPONSE_STATUS_ERROR)
                        .message("Method not found: " + request.getMethodName())
                        .requestId(request.getRequestId())
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
            
            // 其他错误
            return RpcResponse.builder()
                    .status(RpcConstant.RESPONSE_STATUS_ERROR)
                    .message("Error executing method: " + e.getMessage())
                    .requestId(request.getRequestId())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
    
    private Buffer encodeResponse(RpcResponse<?> response) throws Exception {
        // 序列化响应对象
        byte[] data = SerializerFactory.getSerializer(RpcConstant.DEFAULT_SERIALIZER).serialize(response);
        
        // 构建协议头
        Buffer buffer = Buffer.buffer();
        buffer.appendInt(RpcConstant.MAGIC_NUMBER);      // 魔数
        buffer.appendByte(RpcConstant.VERSION);          // 版本号
        buffer.appendByte(RpcConstant.MESSAGE_TYPE_RESPONSE); // 消息类型
        buffer.appendByte((byte) RpcConstant.SERIALIZER_JSON.charAt(0)); // 序列化类型
        buffer.appendInt(data.length);                   // 数据长度
        buffer.appendBytes(data);                        // 响应数据
        
        return buffer;
    }
    
    @Override
    public void shutdown() {
        if (!running) {
            log.warn("Server is not running");
            return;
        }
        
        CompositeFuture.all(
                Future.future(promise -> {
                    if (server != null) {
                        server.close(result -> {
                            log.info("RPC server stopped");
                            promise.complete();
                        });
                    } else {
                        promise.complete();
                    }
                })
        ).onComplete(result -> {
            running = false;
            workerPool.shutdown();
            vertx.close();
        });
    }
    
    @Override
    public void registerService(String serviceInterface, String group, String version, int weight) {
        // 服务注册已经在RpcAutoConfiguration中处理
        // 这里主要是为了接口的完整性
        log.info("Registering service: {}, group: {}, version: {}", serviceInterface, group, version);
    }
    
    @Override
    public void unregisterService(String serviceInterface, String group, String version) {
        String serviceKey = serviceInterface + ":" + group + ":" + version;
        serviceMap.remove(serviceKey);
        log.info("Unregistered service: {}", serviceKey);
    }
    
    @Override
    public Map<String, Object> getServiceMap() {
        return serviceMap;
    }
    
    @Override
    public int getPort() {
        return port;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
}