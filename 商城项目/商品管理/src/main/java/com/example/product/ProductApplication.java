package com.example.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商品管理服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务注册发现
@EnableFeignClients     // 启用Feign客户端
@MapperScan("com.example.product.mapper")  // 扫描MyBatis Mapper接口
@ComponentScan("com.example.product")  // 扫描组件
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}