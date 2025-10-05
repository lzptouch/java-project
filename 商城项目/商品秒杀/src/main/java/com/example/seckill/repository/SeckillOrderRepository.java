package com.example.seckill.repository;

import com.example.seckill.entity.SeckillOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀订单数据访问接口
 */
@Repository
public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {

    /**
     * 根据订单号查询订单
     */
    Optional<SeckillOrder> findByOrderNo(String orderNo);

    /**
     * 根据用户ID和商品ID查询订单
     */
    Optional<SeckillOrder> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * 根据用户ID查询订单列表
     */
    List<SeckillOrder> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据活动ID查询订单列表
     */
    List<SeckillOrder> findByActivityIdAndStatus(Long activityId, Integer status);

    /**
     * 更新订单状态
     */
    @Modifying
    @Transactional
    @Query("update SeckillOrder o set o.status = :status where o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 取消过期订单
     */
    @Modifying
    @Transactional
    @Query("update SeckillOrder o set o.status = 2 where o.status = 0 and o.payDeadline < :now")
    int cancelExpiredOrders(@Param("now") Date now);

    /**
     * 查询用户在指定活动中的订单数量
     */
    int countByUserIdAndActivityId(Long userId, Long activityId);

    /**
     * 根据状态统计订单数量
     */
    int countByStatus(Integer status);
}