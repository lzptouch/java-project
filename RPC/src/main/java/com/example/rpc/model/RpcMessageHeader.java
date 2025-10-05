package com.example.rpc.model;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC消息头
 */
@Data
public class RpcMessageHeader implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 魔数
     */
    private int magicNumber;
    
    /**
     * 协议版本号
     */
    private byte version;
    
    /**
     * 消息类型（请求/响应/心跳）
     */
    private byte messageType;
    
    /**
     * 序列化类型
     */
    private byte serializerType;
    
    /**
     * 状态码（响应时使用）
     */
    private byte statusCode;
    
    /**
     * 消息ID
     */
    private String requestId;
    
    /**
     * 消息体长度
     */
    private int bodyLength;
}