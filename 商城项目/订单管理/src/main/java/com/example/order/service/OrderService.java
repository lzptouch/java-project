package com.example.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.order.entity.OrderMain;
import com.example.order.dto.OrderCreateDTO;
import com.example.order.dto.OrderQueryDTO;
import com.example.order.dto.OrderDetailDTO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<OrderMain> {

    /**
     * 创建订单
     * @param orderCreateDTO 订单创建请求
     * @return 订单号
     */
    Long createOrder(OrderCreateDTO orderCreateDTO);

    /**
     * 获取订单详情
     * @param orderId 订单号
     * @return 订单详情
     */
    OrderDetailDTO getOrderDetail(Long orderId);

    /**
     * 查询订单列表
     * @param queryDTO 查询条件
     * @return 订单列表
     */
    Map<String, Object> getOrderList(OrderQueryDTO queryDTO);

    /**
     * 取消订单
     * @param orderId 订单号
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 支付成功回调
     * @param orderId 订单ID
     * @param payNo 支付单号
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    boolean paySuccess(Long orderId, String payNo, String paymentMethod);

    /**
     * 发货处理
     * @param orderId 订单号
     * @param logisticsNo 物流单号
     * @return 是否成功
     */
    boolean shipOrder(Long orderId, String logisticsNo);

    /**
     * 确认收货
     * @param orderId 订单号
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean confirmReceive(Long orderId, Long userId);

    /**
     * 处理超时订单
     */
    void processTimeoutOrders();
}