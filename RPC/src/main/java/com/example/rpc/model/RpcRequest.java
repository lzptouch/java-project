package com.example.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

/**
 * RPC请求对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 方法名称
     */
    private String methodName;
    
    /**
     * 参数类型数组
     */
    private Class<?>[] parameterTypes;
    
    /**
     * 参数值数组
     */
    private Object[] parameters;
    
    /**
     * 服务版本号
     */
    private String version = "1.0";
    
    /**
     * 分组
     */
    private String group = "default";
    
    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", version='" + version + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}