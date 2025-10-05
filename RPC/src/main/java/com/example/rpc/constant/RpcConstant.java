package com.example.rpc.constant;

/**
 * RPC框架常量类
 */
public class RpcConstant {
    /**
     * 魔数，用于协议安全校验
     */
    public static final int MAGIC_NUMBER = 0xCAFEBABE;
    
    /**
     * 协议版本号
     */
    public static final byte VERSION = 1;
    
    /**
     * 默认主机名
     */
    public static final String DEFAULT_HOST = "localhost";
    
    /**
     * 默认端口号
     */
    public static final int DEFAULT_PORT = 8888;
    
    /**
     * 默认序列化器类型
     */
    public static final String DEFAULT_SERIALIZER = "json";
    
    /**
     * 默认负载均衡器类型
     */
    public static final String DEFAULT_LOAD_BALANCER = "roundRobin";
    
    /**
     * 默认重试策略类型
     */
    public static final String DEFAULT_RETRY_STRATEGY = "fixedInterval";
    
    /**
     * 默认容错策略类型
     */
    public static final String DEFAULT_TOLERANCE_STRATEGY = "failOver";
    
    /**
     * 注册中心默认地址
     */
    public static final String DEFAULT_REGISTRY_ADDRESS = "http://localhost:2379";
    
    /**
     * 注册中心类型 - etcd
     */
    public static final String REGISTRY_TYPE_ETCD = "etcd";
    
    /**
     * 注册中心类型 - zookeeper
     */
    public static final String REGISTRY_TYPE_ZOOKEEPER = "zookeeper";
    
    /**
     * 注册中心类型 - nacos
     */
    public static final String REGISTRY_TYPE_NACOS = "nacos";
    
    /**
     * 服务注册前缀
     */
    public static final String SERVICE_REGISTER_PREFIX = "/rpc/services/";
    
    /**
     * SPI配置文件目录
     */
    public static final String SPI_CONFIG_DIR = "META-INF/rpc";
    
    /**
     * 默认心跳间隔（毫秒）
     */
    public static final long DEFAULT_HEARTBEAT_INTERVAL = 30000;
    
    /**
     * 默认租约时间（毫秒）
     */
    public static final long DEFAULT_LEASE_TTL = 60000;
    
    /**
     * 默认重试间隔（毫秒）
     */
    public static final long DEFAULT_RETRY_INTERVAL = 3000;
    
    /**
     * 默认最大重试次数
     */
    public static final int DEFAULT_MAX_RETRY_COUNT = 2;
    
    /**
     * 消息类型：请求
     */
    public static final byte MESSAGE_TYPE_REQUEST = 1;
    
    /**
     * 消息类型：响应
     */
    public static final byte MESSAGE_TYPE_RESPONSE = 2;
    
    /**
     * 消息类型：心跳
     */
    public static final byte MESSAGE_TYPE_HEARTBEAT = 3;
    
    /**
     * JSON序列化器
     */
    public static final String SERIALIZER_JSON = "json";
    
    /**
     * Kryo序列化器
     */
    public static final String SERIALIZER_KRYO = "kryo";
    
    /**
     * Hessian序列化器
     */
    public static final String SERIALIZER_HESSIAN = "hessian";
    
    /**
     * 轮询负载均衡器
     */
    public static final String LOAD_BALANCER_ROUND_ROBIN = "roundRobin";
    
    /**
     * 随机负载均衡器
     */
    public static final String LOAD_BALANCER_RANDOM = "random";
    
    /**
     * 一致性哈希负载均衡器
     */
    public static final String LOAD_BALANCER_CONSISTENT_HASH = "consistentHash";
    
    /**
     * 固定间隔重试策略
     */
    public static final String RETRY_STRATEGY_FIXED_INTERVAL = "fixedInterval";
    
    /**
     * 指数退避重试策略
     */
    public static final String RETRY_STRATEGY_EXPONENTIAL_BACKOFF = "exponentialBackoff";
    
    /**
     * 故障转移容错策略
     */
    public static final String TOLERANCE_STRATEGY_FAIL_OVER = "failOver";
    
    /**
     * 直接失败容错策略
     */
    public static final String TOLERANCE_STRATEGY_FAIL_FAST = "failFast";
    
    /**
     * 默认超时时间（毫秒）
     */
    public static final long DEFAULT_TIMEOUT = 5000;
    
    /**
     * 默认虚拟节点数量
     */
    public static final int DEFAULT_VIRTUAL_NODE_COUNT = 100;
    
    /**
     * 成功响应码
     */
    public static final int RESPONSE_SUCCESS = 200;
    
    /**
     * 错误响应码
     */
    public static final int RESPONSE_ERROR = 500;
    
    /**
     * 服务未找到响应码
     */
    public static final int RESPONSE_SERVICE_NOT_FOUND = 404;
    
    /**
     * 服务不可用响应码
     */
    public static final int RESPONSE_SERVICE_UNAVAILABLE = 503;
}