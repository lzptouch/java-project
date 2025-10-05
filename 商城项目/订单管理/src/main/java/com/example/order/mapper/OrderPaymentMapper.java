package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.entity.OrderPayment;
import org.apache.ibatis.annotations.Param;

/**
 * 支付记录表Mapper接口
 */
public interface OrderPaymentMapper extends BaseMapper<OrderPayment> {

    /**
     * 根据订单号查询支付记录
     */
    OrderPayment selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据支付流水号查询支付记录
     */
    OrderPayment selectByPayNo(@Param("payNo") String payNo);

    /**
     * 更新支付状态
     */
    int updatePaymentStatus(@Param("id") Long id, @Param("status") Integer status, 
                          @Param("payNo") String payNo, @Param("callbackTime") String callbackTime);
}