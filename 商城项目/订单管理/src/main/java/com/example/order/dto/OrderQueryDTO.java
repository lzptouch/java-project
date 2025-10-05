package com.example.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单查询请求DTO
 */
@Data
public class OrderQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private Long orderId;

    /**
     * 订单状态（0-草稿，1-待支付，2-已支付，3-已发货，4-已完成，5-已取消）
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}