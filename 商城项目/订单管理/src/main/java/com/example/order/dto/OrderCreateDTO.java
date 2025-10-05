package com.example.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建请求DTO
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单商品列表
     */
    private List<OrderItemDTO> items;

    /**
     * 收货地址ID
     */
    private Long addressId;

    /**
     * 支付方式（1-微信，2-支付宝）
     */
    private Integer payType;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 业务类型（1-普通订单，2-秒杀订单，3-团购订单）
     */
    private Integer businessType;

    /**
     * 请求ID（用于幂等性校验）
     */
    private String requestId;

    /**
     * 订单商品DTO内部类
     */
    @Data
    public static class OrderItemDTO {
        
        /**
         * 商品SKU ID
         */
        private Long skuId;
        
        /**
         * 购买数量
         */
        private Integer quantity;
        
        /**
         * 商品单价
         */
        private BigDecimal price;
    }
}