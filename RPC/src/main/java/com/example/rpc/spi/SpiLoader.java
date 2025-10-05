package com.example.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SPI加载器实现
 */
@Slf4j
public class SpiLoader<T> {
    
    /**
     * 单例缓存
     */
    private static final Map<Class<?>, SpiLoader<?>> INSTANCE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 实现类缓存
     */
    private final Map<String, T> instances = new ConcurrentHashMap<>();
    
    /**
     * 类型到实现类的映射
     */
    private final Map<String, Class<? extends T>> classMap = new ConcurrentHashMap<>();
    
    /**
     * SPI接口类型
     */
    private final Class<T> type;
    
    /**
     * 私有构造函数
     */
    private SpiLoader(Class<T> type) {
        this.type = type;
        load();
    }
    
    /**
     * 获取SPI加载器实例
     */
    @SuppressWarnings("unchecked")
    public static <T> SpiLoader<T> getInstance(Class<T> type) {
        SpiLoader<?> loader = INSTANCE_CACHE.get(type);
        if (loader == null) {
            synchronized (SpiLoader.class) {
                loader = INSTANCE_CACHE.get(type);
                if (loader == null) {
                    loader = new SpiLoader<>(type);
                    INSTANCE_CACHE.put(type, loader);
                }
            }
        }
        return (SpiLoader<T>) loader;
    }
    
    /**
     * 加载SPI实现类
     */
    private void load() {
        try {
            String resourcePath = RpcConstant.SPI_CONFIG_DIR + "/" + type.getName();
            Enumeration<URL> urls = ResourceUtil.getResources(resourcePath);
            
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    reader.lines()
                            .filter(line -> !StrUtil.isBlank(line) && !line.startsWith("#"))
                            .forEach(this::processLine);
                }
            }
            
            log.info("Loaded {} SPI implementations for {}", instances.size(), type.getName());
        } catch (Exception e) {
            log.error("Failed to load SPI for {}", type.getName(), e);
            throw new RpcException("Failed to load SPI for " + type.getName(), e);
        }
    }
    
    /**
     * 处理配置行
     */
    private void processLine(String line) {
        try {
            String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                log.warn("Invalid SPI config line: {}", line);
                return;
            }
            
            String key = parts[0].trim();
            String className = parts[1].trim();
            
            Class<?> clazz = Class.forName(className);
            if (!type.isAssignableFrom(clazz)) {
                log.warn("Class {} does not implement {}", className, type.getName());
                return;
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends T> implementationClass = (Class<? extends T>) clazz;
            classMap.put(key, implementationClass);
            
            // 创建实例
            T instance = implementationClass.getDeclaredConstructor().newInstance();
            instances.put(key, instance);
            
            log.debug("Loaded SPI implementation: {}={}", key, className);
        } catch (Exception e) {
            log.error("Failed to load SPI implementation from line: {}", line, e);
        }
    }
    
    /**
     * 根据键获取实例
     */
    public T getInstance(String key) {
        T instance = instances.get(key);
        if (instance == null) {
            throw new IllegalArgumentException("No SPI implementation found for key: " + key);
        }
        return instance;
    }
    
    /**
     * 获取所有键
     */
    public Set<String> getAllKeys() {
        return new HashSet<>(instances.keySet());
    }
    
    /**
     * 获取所有实例
     */
    public Collection<T> getAllInstances() {
        return new ArrayList<>(instances.values());
    }
    
    /**
     * 获取默认实例（使用第一个加载的实例）
     */
    public T getDefaultInstance() {
        if (instances.isEmpty()) {
            throw new IllegalStateException("No SPI implementations loaded for " + type.getName());
        }
        return instances.values().iterator().next();
    }
}