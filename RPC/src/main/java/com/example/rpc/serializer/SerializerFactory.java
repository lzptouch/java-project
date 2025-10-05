package com.example.rpc.serializer;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器工厂
 */
@Slf4j
public class SerializerFactory {
    
    /**
     * 序列化器映射缓存
     */
    private static final Map<String, Serializer> SERIALIZER_MAP = new ConcurrentHashMap<>();
    
    /**
     * 类型到序列化器的映射
     */
    private static final Map<Byte, Serializer> TYPE_TO_SERIALIZER_MAP = new ConcurrentHashMap<>();
    
    static {
        // 通过SPI加载所有序列化器实现
        loadSerializers();
    }
    
    /**
     * 加载序列化器实现
     */
    private static void loadSerializers() {
        try {
            SpiLoader<Serializer> loader = SpiLoader.getInstance(Serializer.class);
            for (String key : loader.getAllKeys()) {
                Serializer serializer = loader.getInstance(key);
                SERIALIZER_MAP.put(key, serializer);
                TYPE_TO_SERIALIZER_MAP.put(serializer.getType(), serializer);
                log.info("Loaded serializer: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to load serializers", e);
        }
    }
    
    /**
     * 获取序列化器
     */
    public static Serializer getSerializer(String name) {
        Serializer serializer = SERIALIZER_MAP.get(name);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown serializer: " + name);
        }
        return serializer;
    }
    
    /**
     * 根据类型获取序列化器
     */
    public static Serializer getSerializerByType(byte type) {
        Serializer serializer = TYPE_TO_SERIALIZER_MAP.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown serializer type: " + type);
        }
        return serializer;
    }
    
    /**
     * 获取默认序列化器
     */
    public static Serializer getDefaultSerializer() {
        return getSerializer(RpcConstant.DEFAULT_SERIALIZER);
    }
}