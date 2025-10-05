package com.example.rpc.annotation;

import com.example.rpc.config.RpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用RPC框架注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(RpcAutoConfiguration.class)
public @interface EnableRpc {
    
    /**
     * 是否启用服务端
     */
    boolean enableServer() default true;
    
    /**
     * 是否启用客户端
     */
    boolean enableClient() default true;
    
    /**
     * 注册中心地址
     */
    String registryAddress() default "http://localhost:2379";
    
    /**
     * 注册中心类型
     */
    String registryType() default "etcd";
}