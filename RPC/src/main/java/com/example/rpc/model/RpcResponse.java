package com.example.rpc.model;

import com.example.rpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC响应对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应ID，与请求ID对应
     */
    private String requestId;
    
    /**
     * 响应状态
     * 0: 成功
     * 非0: 失败
     */
    private int status = RpcConstant.RESPONSE_STATUS_SUCCESS;
    
    /**
     * 错误信息，当status非0时使用
     */
    private String message;
    
    /**
     * 响应结果，当status为0时使用
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private long timestamp = System.currentTimeMillis();
    
    /**
     * 创建成功响应
     */
    public static <T> RpcResponse<T> success(String requestId, T data) {
        return RpcResponse.<T>builder()
                .requestId(requestId)
                .status(RpcConstant.RESPONSE_STATUS_SUCCESS)
                .data(data)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static <T> RpcResponse<T> failure(String requestId, String message) {
        return RpcResponse.<T>builder()
                .requestId(requestId)
                .status(RpcConstant.RESPONSE_STATUS_FAILURE)
                .message(message)
                .build();
    }
}