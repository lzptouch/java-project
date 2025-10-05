package com.example.rpc.serializer.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * JSON序列化器
 * 使用Fastjson2库实现对象的JSON序列化和反序列化
 */
@Slf4j
public class JsonSerializer implements Serializer {
    
    @Override
    public <T> byte[] serialize(T object) {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object);
        } catch (JSONException e) {
            log.error("Failed to serialize object to JSON: {}", object.getClass().getName(), e);
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(bytes, clazz);
        } catch (JSONException e) {
            log.error("Failed to deserialize JSON to object: {}", clazz.getName(), e);
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }
    
    @Override
    public byte getType() {
        return 0x01; // JSON序列化器类型标识
    }
    
    @Override
    public String getName() {
        return RpcConstant.SERIALIZER_JSON;
    }
}