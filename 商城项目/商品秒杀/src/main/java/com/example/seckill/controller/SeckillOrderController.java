package com.example.seckill.controller;

import com.example.seckill.entity.SeckillOrder;
import com.example.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀订单控制器
 */
@RestController
@RequestMapping("/api/seckill/orders")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService orderService;

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        Optional<SeckillOrder> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据订单号获取订单
     */
    @GetMapping("/orderNo/{orderNo}")
    public ResponseEntity<?> getOrderByOrderNo(@PathVariable String orderNo) {
        Optional<SeckillOrder> order = orderService.getOrderByOrderNo(orderNo);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取用户的订单列表
     */
    @GetMapping("/user/{userId}")
    public List<SeckillOrder> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean updated = orderService.updateOrderStatus(id, status);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}