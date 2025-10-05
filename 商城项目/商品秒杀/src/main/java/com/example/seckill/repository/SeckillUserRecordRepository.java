package com.example.seckill.repository;

import com.example.seckill.entity.SeckillUserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;

/**
 * 秒杀用户抢购记录数据访问接口
 */
@Repository
public interface SeckillUserRecordRepository extends JpaRepository<SeckillUserRecord, Long> {

    /**
     * 检查用户是否已抢购过指定商品
     */
    boolean existsByUserIdAndProductIdAndActivityId(Long userId, Long productId, Long activityId);

    /**
     * 统计用户在指定活动中的抢购次数
     */
    int countByUserIdAndActivityId(Long userId, Long activityId);

    /**
     * 删除指定活动的用户抢购记录
     */
    @Modifying
    @Transactional
    @Query("delete from SeckillUserRecord r where r.activityId = :activityId")
    int deleteByActivityId(@Param("activityId") Long activityId);

    /**
     * 批量删除过期记录
     */
    @Modifying
    @Transactional
    @Query("delete from SeckillUserRecord r where r.createTime < :deadline")
    int deleteExpiredRecords(@Param("deadline") java.util.Date deadline);
}