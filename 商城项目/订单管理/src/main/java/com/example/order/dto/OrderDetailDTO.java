package com.example.order.dto;

import com.example.order.entity.OrderAfterSale;
import com.example.order.entity.OrderItem;
import com.example.order.entity.OrderPayment;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单详情响应DTO
 */
@Data
public class OrderDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单状态文本
     */
    private String statusText;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 发货时间
     */
    private Date shipTime;

    /**
     * 签收时间
     */
    private Date receiveTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 订单商品列表
     */
    private List<OrderItem> orderItems;

    /**
     * 支付信息
     */
    private OrderPayment payment;

    /**
     * 售后信息
     */
    private OrderAfterSale afterSale;

    /**
     * 收货地址信息
     */
    private AddressDTO address;

    /**
     * 收货地址DTO内部类
     */
    @Data
    public static class AddressDTO {
        
        /**
         * 收货人姓名
         */
        private String receiverName;
        
        /**
         * 收货人手机号
         */
        private String receiverPhone;
        
        /**
         * 收货地址
         */
        private String fullAddress;
    }
}