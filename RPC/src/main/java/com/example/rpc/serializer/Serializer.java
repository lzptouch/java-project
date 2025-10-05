package com.example.rpc.serializer;

import java.io.IOException;

/**
 * 序列化器接口
 */
public interface Serializer {
    /**
     * 序列化对象为字节数组
     */
    <T> byte[] serialize(T object) throws IOException;
    
    /**
     * 反序列化字节数组为对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;
    
    /**
     * 获取序列化器类型
     */
    byte getType();
    
    /**
     * 获取序列化器名称
     */
    String getName();
}