package com.example.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON序列化器实现
 */
public class JsonSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(bytes, clazz);
    }
    
    @Override
    public byte getType() {
        return 1; // JSON序列化器类型标识
    }
    
    @Override
    public String getName() {
        return "json";
    }
}