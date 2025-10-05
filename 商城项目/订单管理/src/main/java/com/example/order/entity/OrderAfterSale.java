package com.example.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 售后记录表实体
 */
@Data
@TableName("order_after_sale")
public class OrderAfterSale {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 售后单号
     */
    private String afterSaleNo;

    /**
     * 关联订单号
     */
    private Long orderId;

    /**
     * 售后类型（1-退款，2-退货退款，3-换货）
     */
    private Integer type;

    /**
     * 售后状态（0-申请中，1-审核通过，2-审核拒绝，3-处理中，4-已完成）
     */
    private Integer status;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 处理时间
     */
    private Date processTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}