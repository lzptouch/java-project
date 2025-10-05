package com.example.seckill.repository;

import com.example.seckill.entity.SeckillActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * 秒杀活动数据访问接口
 */
@Repository
public interface SeckillActivityRepository extends JpaRepository<SeckillActivity, Long> {

    /**
     * 查找未开始的活动
     */
    List<SeckillActivity> findByStartTimeAfterAndStatus(Date now, Integer status);

    /**
     * 查找进行中的活动
     */
    @Query("select a from SeckillActivity a where a.startTime <= :now and a.endTime >= :now and a.status = :status")
    List<SeckillActivity> findActiveActivities(@Param("now") Date now, @Param("status") Integer status);

    /**
     * 查找已结束的活动
     */
    List<SeckillActivity> findByEndTimeBeforeAndStatus(Date now, Integer status);

    /**
     * 更新活动状态
     */
    @Modifying
    @Transactional
    @Query("update SeckillActivity a set a.status = :status where a.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新过期活动状态
     */
    @Modifying
    @Transactional
    @Query("update SeckillActivity a set a.status = 2 where a.endTime < :now and a.status = 1")
    int updateExpiredStatus(@Param("now") Date now);

    /**
     * 更新开始活动状态
     */
    @Modifying
    @Transactional
    @Query("update SeckillActivity a set a.status = 1 where a.startTime <= :now and a.endTime >= :now and a.status = 0")
    int updateStartedStatus(@Param("now") Date now);
}