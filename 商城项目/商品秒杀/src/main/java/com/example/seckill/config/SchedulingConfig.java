package com.example.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 启用Spring的定时任务支持
}