package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.entity.OrderMain;
import com.example.order.dto.OrderQueryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单主表Mapper接口
 */
public interface OrderMainMapper extends BaseMapper<OrderMain> {

    /**
     * 根据条件查询订单列表
     */
    List<OrderMain> selectOrderList(@Param("query") OrderQueryDTO query);

    /**
     * 根据订单号查询订单详情（包含明细）
     */
    OrderMain selectOrderDetailByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新订单状态
     */
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") Integer status, 
                         @Param("oldStatus") Integer oldStatus);

    /**
     * 查询超时未支付的订单
     */
    List<OrderMain> selectTimeoutOrders(@Param("timeoutMinutes") int timeoutMinutes);
}