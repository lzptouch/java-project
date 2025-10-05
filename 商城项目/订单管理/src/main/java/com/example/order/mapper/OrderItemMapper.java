package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细表Mapper接口
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单号查询订单明细
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 批量插入订单明细
     */
    int batchInsert(@Param("items") List<OrderItem> items);
}