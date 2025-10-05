package com.example.order.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置类
 */
@Configuration
@EnableFeignClients(basePackages = "com.example.order.feign")
public class FeignConfig {
}