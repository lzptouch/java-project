package com.example.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian序列化器实现
 */
public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            HessianOutput output = new HessianOutput(bos);
            output.writeObject(object);
            return bos.toByteArray();
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            HessianInput input = new HessianInput(bis);
            @SuppressWarnings("unchecked")
            T result = (T) input.readObject();
            return result;
        }
    }
    
    @Override
    public byte getType() {
        return 2; // Hessian序列化器类型标识
    }
    
    @Override
    public String getName() {
        return "hessian";
    }
}