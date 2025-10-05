package com.example.seckill.service;

import com.example.seckill.entity.SeckillOrder;
import com.example.seckill.repository.SeckillOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 秒杀订单服务类
 */
@Service
public class SeckillOrderService {

    @Autowired
    private SeckillOrderRepository orderRepository;

    /**
     * 根据ID获取订单
     */
    @Cacheable(value = "seckillOrder", key = "#id")
    public Optional<SeckillOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * 根据订单号获取订单
     */
    @Cacheable(value = "seckillOrder", key = "#orderNo")
    public Optional<SeckillOrder> getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    /**
     * 根据用户ID和商品ID获取订单
     */
    public Optional<SeckillOrder> getOrderByUserIdAndProductId(Long userId, Long productId) {
        return orderRepository.findByUserIdAndProductId(userId, productId);
    }

    /**
     * 获取用户的订单列表
     */
    public List<SeckillOrder> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 创建订单
     */
    @Transactional
    public SeckillOrder createOrder(SeckillOrder order) {
        // 生成订单号
        order.setOrderNo(generateOrderNo());
        // 设置默认状态
        order.setStatus(0); // 待支付
        // 设置支付截止时间（例如：15分钟后）
        Date now = new Date();
        Date payDeadline = new Date(now.getTime() + 15 * 60 * 1000);
        order.setPayDeadline(payDeadline);
        return orderRepository.save(order);
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public boolean updateOrderStatus(Long id, Integer status) {
        return orderRepository.updateStatus(id, status) > 0;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 生成基于时间戳和随机数的订单号
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).replace("-", "");
        return "SK" + timestamp + random;
    }

    /**
     * 检查用户在活动中是否已购买
     */
    public boolean hasUserPurchasedInActivity(Long userId, Long activityId) {
        return orderRepository.countByUserIdAndActivityId(userId, activityId) > 0;
    }

    /**
     * 定时任务：取消过期订单
     */
    @Scheduled(cron = "0/30 * * * * ?") // 每30秒执行一次
    @Transactional
    public void cancelExpiredOrders() {
        Date now = new Date();
        int count = orderRepository.cancelExpiredOrders(now);
        if (count > 0) {
            System.out.println("取消了" + count + "个过期订单");
        }
    }
}