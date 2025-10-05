package com.example.rpc;

import com.example.rpc.annotation.RpcReference;
import com.example.rpc.annotation.RpcService;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.proxy.ServiceProxyFactory;
import com.example.rpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * RPC框架演示启动类
 */
@SpringBootApplication
public class RpcFrameworkDemo {

    public static void main(String[] args) {
        SpringApplication.run(RpcFrameworkDemo.class, args);
    }
    
    /**
     * 测试服务接口
     */
    public interface EchoService {
        String echo(String message);
    }
    
    /**
     * 测试服务实现
     */
    @RpcService(group = "demo", version = "1.0")
    @Component
    public static class EchoServiceImpl implements EchoService {
        @Override
        public String echo(String message) {
            return "Echo: " + message;
        }
    }
    
    /**
     * 测试客户端
     */
    @Component
    public static class EchoClient {
        
        @RpcReference(group = "demo", version = "1.0")
        private EchoService echoService;
        
        public String callEcho(String message) {
            return echoService.echo(message);
        }
    }
    
    /**
     * 演示RPC调用
     */
    @Bean
    public CommandLineRunner demoRunner(@Autowired EchoClient echoClient, 
                                       @Autowired ApplicationContext context) {
        return args -> {
            // 等待服务器启动
            Thread.sleep(1000);
            
            System.out.println("\n===== RPC框架演示 =====");
            
            // 调用RPC服务
            String result = echoClient.callEcho("Hello RPC");
            System.out.println("客户端调用结果: " + result);
            
            System.out.println("\nRPC框架启动成功!\n");
        };
    }
}