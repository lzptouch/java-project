package com.example.rpc.serializer;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.model.RpcRequest;
import com.example.rpc.model.RpcResponse;
import com.example.rpc.serializer.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * 序列化器测试类
 */
@Slf4j
public class SerializerTest {
    
    /**
     * 测试JSON序列化器
     */
    @Test
    public void testJsonSerializer() {
        Serializer serializer = new JsonSerializer();
        assertEquals(0x01, serializer.getType());
        assertEquals(RpcConstant.SERIALIZER_JSON, serializer.getName());
        
        // 测试序列化和反序列化RpcRequest对象
        RpcRequest request = RpcRequest.builder()
                .requestId("123456")
                .serviceName("com.example.rpc.test.TestService")
                .methodName("testMethod")
                .parameterTypes(new Class[]{String.class, Integer.class})
                .parameters(new Object[]{"test", 123})
                .version("1.0")
                .group("default")
                .build();
        
        byte[] requestBytes = serializer.serialize(request);
        assertNotNull(requestBytes);
        assertTrue(requestBytes.length > 0);
        
        RpcRequest deserializedRequest = serializer.deserialize(requestBytes, RpcRequest.class);
        assertNotNull(deserializedRequest);
        assertEquals(request.getRequestId(), deserializedRequest.getRequestId());
        assertEquals(request.getServiceName(), deserializedRequest.getServiceName());
        assertEquals(request.getMethodName(), deserializedRequest.getMethodName());
        assertArrayEquals(request.getParameterTypes(), deserializedRequest.getParameterTypes());
        assertArrayEquals(request.getParameters(), deserializedRequest.getParameters());
        assertEquals(request.getVersion(), deserializedRequest.getVersion());
        assertEquals(request.getGroup(), deserializedRequest.getGroup());
        
        // 测试序列化和反序列化RpcResponse对象
        RpcResponse<String> response = RpcResponse.<String>builder()
                .requestId("123456")
                .status(RpcConstant.RESPONSE_STATUS_SUCCESS)
                .message(null)
                .data("Success Result")
                .timestamp(System.currentTimeMillis())
                .build();
        
        byte[] responseBytes = serializer.serialize(response);
        assertNotNull(responseBytes);
        assertTrue(responseBytes.length > 0);
        
        RpcResponse<?> deserializedResponse = serializer.deserialize(responseBytes, RpcResponse.class);
        assertNotNull(deserializedResponse);
        assertEquals(response.getRequestId(), deserializedResponse.getRequestId());
        assertEquals(response.getStatus(), deserializedResponse.getStatus());
        assertEquals(response.getData(), deserializedResponse.getData());
        
        log.info("JSON serializer test passed successfully!");
    }
    
    /**
     * 测试序列化器工厂
     */
    @Test
    public void testSerializerFactory() {
        // 测试根据名称获取序列化器
        Serializer jsonSerializer = SerializerFactory.getSerializer(RpcConstant.SERIALIZER_JSON);
        assertNotNull(jsonSerializer);
        assertEquals("json", jsonSerializer.getName());
        
        // 测试获取默认序列化器
        Serializer defaultSerializer = SerializerFactory.getDefaultSerializer();
        assertNotNull(defaultSerializer);
        assertEquals(jsonSerializer.getName(), defaultSerializer.getName());
        
        // 测试根据类型获取序列化器
        Serializer typeSerializer = SerializerFactory.getSerializer(defaultSerializer.getType());
        assertNotNull(typeSerializer);
        assertEquals(defaultSerializer.getName(), typeSerializer.getName());
        
        log.info("Serializer factory test passed successfully!");
    }
}