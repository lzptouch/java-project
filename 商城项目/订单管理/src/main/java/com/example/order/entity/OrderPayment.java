package com.example.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录表实体
 */
@Data
@TableName("order_payment")
public class OrderPayment {

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
     * 支付渠道流水号（如微信支付的transaction_id）
     */
    private String payNo;

    /**
     * 支付方式（wechat-微信，alipay-支付宝等）
     */
    private String payType;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付状态（0-待支付，1-支付成功，2-支付失败）
     */
    private Integer status;

    /**
     * 支付回调时间
     */
    private Date callbackTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}