package com.example.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单主表实体
 */
@Data
@TableName("order_main")
public class OrderMain {

    /**
     * 订单号（雪花算法生成）
     */
    @TableId
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
     * 实付金额（扣除优惠后）
     */
    private BigDecimal payAmount;

    /**
     * 订单状态（0-草稿，1-待支付，2-已支付，3-已发货，4-已完成，5-已取消）
     */
    private Integer status;

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
     * 更新时间
     */
    private Date updateTime;

    /**
     * 订单明细列表
     */
    @TableField(exist = false)
    private List<OrderItem> orderItems;

    /**
     * 支付记录
     */
    @TableField(exist = false)
    private OrderPayment payment;

    /**
     * 售后记录
     */
    @TableField(exist = false)
    private OrderAfterSale afterSale;
}