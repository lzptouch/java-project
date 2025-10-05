package com.example.rpc.test;

import com.example.rpc.annotation.RpcService;
import com.example.rpc.client.RpcClient;
import com.example.rpc.client.RpcClientFactory;
import com.example.rpc.config.RpcProperties;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.registry.RegistryFactory;
import com.example.rpc.registry.ServiceRegistry;
import com.example.rpc.server.RpcServer;
import com.example.rpc.server.RpcServerImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RPC框架集成测试
 */
@Slf4j
@SpringJUnitConfig
public class RpcIntegrationTest {

    private static final int TEST_PORT = 8888;
    private static final String TEST_SERVICE_KEY = "com.example.rpc.test.TestService:default:1.0";
    
    private RpcServer server;
    private RpcClient client;
    private ServiceRegistry registry;
    
    @BeforeEach
    public void setUp() {
        // 创建服务注册表
        registry = RegistryFactory.createRegistry("etcd", "http://localhost:2379");
        
        // 创建服务映射并添加测试服务
        ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>();
        serviceMap.put(TEST_SERVICE_KEY, new TestServiceImpl());
        
        // 创建并启动服务器
        server = new RpcServerImpl(TEST_PORT, registry, serviceMap);
        server.start();
        
        // 创建客户端
        client = RpcClientFactory.getInstance().createClient("vertx");
        client.init();
    }
    
    @AfterEach
    public void tearDown() {
        // 关闭客户端和服务器
        client.close();
        server.shutdown();
        registry.close();
    }
    
    @Test
    public void testRpcCall() throws ExecutionException, InterruptedException, TimeoutException {
        // 创建请求
        RpcRequest request = RpcRequest.builder()
                .requestId("test-request-123")
                .serviceName("com.example.rpc.test.TestService")
                .methodName("sayHello")
                .parameterTypes(new Class[]{String.class})
                .parameters(new Object[]{"World"})
                .group("default")
                .version("1.0")
                .build();
        
        // 发送请求到本地服务器
        CompletableFuture<RpcResponse> future = client.sendRequest("localhost", TEST_PORT, request);
        
        // 等待响应并验证
        RpcResponse response = future.get(5, TimeUnit.SECONDS);
        
        log.info("Response: {}", response);
        
        // 验证响应是否成功
        Assertions.assertEquals(200, response.getStatus(), "Response status should be 200 (success)");
        Assertions.assertNotNull(response.getData(), "Response data should not be null");
        Assertions.assertEquals("Hello, World", response.getData(), "Response data should match expected result");
        Assertions.assertEquals("test-request-123", response.getRequestId(), "Request ID should be preserved");
    }
    
    @Test
    public void testRpcCallWithInvalidService() throws ExecutionException, InterruptedException {
        // 创建请求，使用不存在的服务
        RpcRequest request = RpcRequest.builder()
                .requestId("test-request-456")
                .serviceName("com.example.rpc.test.NonExistentService")
                .methodName("someMethod")
                .parameterTypes(new Class[]{})
                .parameters(new Object[]{})
                .group("default")
                .version("1.0")
                .build();
        
        // 发送请求到本地服务器
        CompletableFuture<RpcResponse> future = client.sendRequest("localhost", TEST_PORT, request);
        
        // 等待响应并验证错误
        RpcResponse response = future.get();
        
        log.info("Error response: {}", response);
        
        // 验证响应是否包含错误
        Assertions.assertEquals(500, response.getStatus(), "Response status should be 500 (error)");
        Assertions.assertTrue(response.getMessage().contains("Service not found"), 
                "Response should contain 'Service not found' message");
    }
    
    /**
     * 测试服务接口
     */
    public interface TestService {
        String sayHello(String name);
    }
    
    /**
     * 测试服务实现
     */
    public static class TestServiceImpl implements TestService {
        @Override
        public String sayHello(String name) {
            return "Hello, " + name;
        }
    }
}