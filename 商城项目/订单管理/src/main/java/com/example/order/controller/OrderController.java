package com.example.order.controller;

import com.example.order.service.OrderService;
import com.example.order.dto.OrderCreateDTO;
import com.example.order.dto.OrderQueryDTO;
import com.example.order.dto.OrderDetailDTO;
import com.example.order.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@Api(tags = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping
    public Result<Long> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        log.info("创建订单请求: {}", orderCreateDTO);
        try {
            Long orderId = orderService.createOrder(orderCreateDTO);
            return Result.success(orderId);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderDetailDTO> getOrderDetail(@PathVariable Long orderId) {
        log.info("获取订单详情请求: orderId={}", orderId);
        try {
            OrderDetailDTO detailDTO = orderService.getOrderDetail(orderId);
            return Result.success(detailDTO);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("查询订单列表")
    @GetMapping
    public Result<Map<String, Object>> getOrderList(OrderQueryDTO queryDTO) {
        log.info("查询订单列表请求: {}", queryDTO);
        try {
            Map<String, Object> result = orderService.getOrderList(queryDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("取消订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        log.info("取消订单请求: orderId={}, userId={}", orderId, userId);
        try {
            boolean result = orderService.cancelOrder(orderId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("支付成功回调")
    @PostMapping("/{orderId}/pay-success")
    public Result<Boolean> paySuccess(@PathVariable Long orderId, 
                                    @RequestParam String payNo, 
                                    @RequestParam String paymentMethod) {
        log.info("支付成功回调请求: orderId={}, payNo={}, paymentMethod={}", orderId, payNo, paymentMethod);
        try {
            boolean result = orderService.paySuccess(orderId, payNo, paymentMethod);
            return Result.success(result);
        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("发货")
    @PutMapping("/{orderId}/ship")
    public Result<Boolean> shipOrder(@PathVariable Long orderId, @RequestParam String logisticsNo) {
        log.info("订单发货请求: orderId={}, logisticsNo={}", orderId, logisticsNo);
        try {
            boolean result = orderService.shipOrder(orderId, logisticsNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("订单发货失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("确认收货")
    @PutMapping("/{orderId}/confirm-receive")
    public Result<Boolean> confirmReceive(@PathVariable Long orderId, @RequestParam Long userId) {
        log.info("确认收货请求: orderId={}, userId={}", orderId, userId);
        try {
            boolean result = orderService.confirmReceive(orderId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return Result.error(e.getMessage());
        }
    }
}