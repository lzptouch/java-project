package com.example.rpc.model;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC消息封装类
 */
@Data
public class RpcMessage<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息头
     */
    private RpcMessageHeader header;
    
    /**
     * 消息体（请求或响应）
     */
    private T body;
}