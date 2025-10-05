package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.entity.OrderAfterSale;
import org.apache.ibatis.annotations.Param;

/**
 * 售后记录表Mapper接口
 */
public interface OrderAfterSaleMapper extends BaseMapper<OrderAfterSale> {

    /**
     * 根据订单号查询售后记录
     */
    OrderAfterSale selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据售后单号查询售后记录
     */
    OrderAfterSale selectByAfterSaleNo(@Param("afterSaleNo") String afterSaleNo);

    /**
     * 更新售后状态
     */
    int updateAfterSaleStatus(@Param("id") Long id, @Param("status") Integer status);
}