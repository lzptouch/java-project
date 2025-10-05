package com.example.order.task;

import com.example.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单定时任务
 */
@Slf4j
@Component
public class OrderScheduledTask {

    @Autowired
    private OrderService orderService;

    /**
     * 每5分钟执行一次，处理超时未支付的订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processTimeoutOrders() {
        log.info("定时任务开始执行：处理超时订单");
        orderService.processTimeoutOrders();
        log.info("定时任务执行完成：处理超时订单");
    }
}