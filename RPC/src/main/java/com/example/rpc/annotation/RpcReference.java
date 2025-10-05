package com.example.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC引用注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    
    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;
    
    /**
     * 服务版本号
     */
    String version() default "1.0";
    
    /**
     * 服务分组
     */
    String group() default "default";
    
    /**
     * 序列化器类型
     */
    String serializer() default "json";
    
    /**
     * 负载均衡器类型
     */
    String loadBalancer() default "roundRobin";
    
    /**
     * 重试策略类型
     */
    String retryStrategy() default "fixedInterval";
    
    /**
     * 容错策略类型
     */
    String toleranceStrategy() default "failOver";
    
    /**
     * 最大重试次数
     */
    int maxRetryCount() default 2;
}