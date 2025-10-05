package com.example.rpc.proxy;

import com.example.rpc.client.RpcClient;
import com.example.rpc.client.RpcClientFactory;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.registry.RegistryFactory;
import com.example.rpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 服务代理工厂，用于创建远程服务的代理对象
 */
@Slf4j
public class ServiceProxyFactory {
    
    private final ServiceRegistry registry;
    
    public ServiceProxyFactory() {
        this.registry = RegistryFactory.getRegistry();
    }
    
    /**
     * 创建服务代理
     * @param serviceInterface 服务接口类
     * @param version 版本号
     * @param group 分组
     * @return 服务代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> serviceInterface, String version, String group) {
        log.info("Creating proxy for service: {}, version: {}, group: {}", 
                serviceInterface.getName(), version, group);
        
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new ServiceInvocationHandler(serviceInterface, version, group)
        );
    }
    
    /**
     * 服务调用处理器
     */
    private static class ServiceInvocationHandler implements InvocationHandler {
        
        private final Class<?> serviceInterface;
        private final String version;
        private final String group;
        private final RpcClient client;
        
        public ServiceInvocationHandler(Class<?> serviceInterface, String version, String group) {
            this.serviceInterface = serviceInterface;
            this.version = version != null && !version.isEmpty() ? version : RpcConstant.DEFAULT_VERSION;
            this.group = group != null && !group.isEmpty() ? group : RpcConstant.DEFAULT_GROUP;
            this.client = RpcClientFactory.getDefaultClient();
            
            // 初始化客户端
            if (!client.isInitialized()) {
                client.init();
            }
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 如果是Object类的方法，直接调用本地实现
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            
            String methodName = method.getName();
            String serviceName = serviceInterface.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            
            log.debug("Invoking remote method: {} on service: {} (version: {}, group: {})", 
                    methodName, serviceName, version, group);
            
            // 创建RPC请求
            RpcRequest request = RpcRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .serviceName(serviceName)
                    .methodName(methodName)
                    .parameterTypes(parameterTypes)
                    .parameters(args)
                    .version(version)
                    .group(group)
                    .build();
            
            // 发送请求，处理CompletableFuture响应
            RpcResponse<?> response;
            try {
                CompletableFuture<RpcResponse> futureResponse = client.sendRequest(request);
                // 等待响应，设置超时（假设客户端有默认超时设置）
                response = futureResponse.get(3000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("RPC call failed: {}.{}()", serviceName, methodName, e);
                throw new RuntimeException("RPC call failed: " + e.getMessage(), e);
            }
            
            // 处理响应
            if (response == null) {
                throw new RuntimeException("Received null response for request: " + request.getRequestId());
            }
            
            if (response.getStatus() != RpcConstant.RESPONSE_STATUS_SUCCESS) {
                log.error("RPC call failed with status: {}, message: {}", 
                        response.getStatus(), response.getMessage());
                throw new RuntimeException(response.getMessage() != null ? 
                        response.getMessage() : "RPC call failed with status: " + response.getStatus());
            }
            
            return response.getData();
        }
    }
}