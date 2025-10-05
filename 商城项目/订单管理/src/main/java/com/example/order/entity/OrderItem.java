package com.example.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细表实体
 */
@Data
@TableName("order_item")
public class OrderItem {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 关联订单号
     */
    private Long orderId;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品名称（下单时快照，避免商品改名影响订单）
     */
    private String productName;

    /**
     * SKU规格（如颜色:红;尺码:L）
     */
    private String skuSpecs;

    /**
     * 下单时单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 明细总金额（price*quantity）
     */
    private BigDecimal totalPrice;

    /**
     * 商品图片URL
     */
    @TableField(exist = false)
    private String productImage;
}