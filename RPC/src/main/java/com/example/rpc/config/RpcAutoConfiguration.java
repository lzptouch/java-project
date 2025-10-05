package com.example.rpc.config;

import com.example.rpc.annotation.RpcReference;
import com.example.rpc.annotation.RpcService;
import com.example.rpc.proxy.ServiceProxyFactory;
import com.example.rpc.registry.RegistryFactory;
import com.example.rpc.registry.ServiceRegistry;
import com.example.rpc.server.RpcServer;
import com.example.rpc.server.RpcServerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC框架自动配置类
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
@Slf4j
public class RpcAutoConfiguration {
    
    @Autowired
    private RpcProperties properties;
    
    /**
     * 本地服务注册表
     */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    
    /**
     * 创建服务代理工厂
     */
    @Bean
    public ServiceProxyFactory serviceProxyFactory() {
        return new ServiceProxyFactory();
    }
    
    /**
     * 创建注册中心实例
     */
    @Bean
    public ServiceRegistry serviceRegistry() {
        ServiceRegistry registry = RegistryFactory.createRegistry(
                properties.getRegistryType(),
                properties.getRegistryAddress());
        
        // 注册JVM关闭钩子，在应用关闭时注销服务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down RPC registry...");
            registry.close();
        }));
        
        return registry;
    }
    
    /**
     * 创建RPC服务器
     */
    @Bean
    public RpcServer rpcServer(@Autowired ServiceRegistry registry) {
        RpcServer server = new RpcServerImpl(properties.getServerPort(), registry, serviceMap);
        
        // 启动服务器
        server.start();
        
        // 注册JVM关闭钩子，在应用关闭时停止服务器
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down RPC server...");
            server.shutdown();
        }));
        
        return server;
    }
    
    /**
     * 服务导出和引用的BeanPostProcessor
     */
    @Bean
    public BeanPostProcessor rpcBeanPostProcessor(@Autowired ServiceProxyFactory proxyFactory,
                                                 @Autowired RpcServer rpcServer) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 处理@RpcService注解的服务实现类
                RpcService rpcService = AnnotationUtils.getAnnotation(bean.getClass(), RpcService.class);
                if (rpcService != null) {
                    exportService(bean, rpcService);
                }
                return bean;
            }
            
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                // 处理@RpcReference注解的字段
                Class<?> beanClass = bean.getClass();
                for (Field field : ReflectionUtils.getAllDeclaredFields(beanClass)) {
                    RpcReference rpcReference = AnnotationUtils.getAnnotation(field, RpcReference.class);
                    if (rpcReference != null) {
                        injectReference(bean, field, rpcReference);
                    }
                }
                return bean;
            }
            
            /**
             * 导出服务
             */
            private void exportService(Object bean, RpcService rpcService) {
                try {
                    Class<?> interfaceClass = rpcService.interfaceClass();
                    if (interfaceClass == void.class) {
                        // 如果没有指定接口类，使用第一个实现的接口
                        interfaceClass = bean.getClass().getInterfaces()[0];
                    }
                    
                    String serviceKey = interfaceClass.getName() + ":" + rpcService.group() + ":" + rpcService.version();
                    serviceMap.put(serviceKey, bean);
                    
                    // 注册服务到注册中心
                    rpcServer.registerService(interfaceClass.getName(), rpcService.group(), 
                            rpcService.version(), rpcService.weight());
                    
                    log.info("Exported service: {} with key: {}", interfaceClass.getName(), serviceKey);
                } catch (Exception e) {
                    log.error("Failed to export service: {}", bean.getClass().getName(), e);
                }
            }
            
            /**
             * 注入服务引用
             */
            private void injectReference(Object bean, Field field, RpcReference rpcReference) {
                try {
                    Class<?> interfaceClass = rpcReference.interfaceClass();
                    if (interfaceClass == void.class) {
                        interfaceClass = field.getType();
                    }
                    
                    // 创建代理对象
                    Object proxy = proxyFactory.createProxy(interfaceClass, 
                            rpcReference.version(), rpcReference.group());
                    
                    // 设置字段可访问并注入代理
                    field.setAccessible(true);
                    field.set(bean, proxy);
                    
                    log.info("Injected reference: {} into field: {}", interfaceClass.getName(), field.getName());
                } catch (Exception e) {
                    log.error("Failed to inject reference into field: {}", field.getName(), e);
                }
            }
        };
    }
}