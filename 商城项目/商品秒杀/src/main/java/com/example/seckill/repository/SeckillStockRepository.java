package com.example.seckill.repository;

import com.example.seckill.entity.SeckillStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;

/**
 * 秒杀库存数据访问接口
 */
@Repository
public interface SeckillStockRepository extends JpaRepository<SeckillStock, Long> {

    /**
     * 根据秒杀商品ID查询库存（悲观锁）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    SeckillStock findBySeckillProductId(Long seckillProductId);

    /**
     * 乐观锁扣减库存
     */
    @Modifying
    @Transactional
    @Query("update SeckillStock s set s.quantity = s.quantity - 1, s.version = s.version + 1 " +
           "where s.seckillProductId = :seckillProductId and s.quantity > 0 and s.version = :version")
    int deductStockWithOptimisticLock(@Param("seckillProductId") Long seckillProductId, @Param("version") Integer version);

    /**
     * 直接扣减库存（用于测试）
     */
    @Modifying
    @Transactional
    @Query("update SeckillStock s set s.quantity = s.quantity - :quantity " +
           "where s.seckillProductId = :seckillProductId and s.quantity >= :quantity")
    int deductStock(@Param("seckillProductId") Long seckillProductId, @Param("quantity") Integer quantity);

    /**
     * 增加库存
     */
    @Modifying
    @Transactional
    @Query("update SeckillStock s set s.quantity = s.quantity + :quantity " +
           "where s.seckillProductId = :seckillProductId")
    int addStock(@Param("seckillProductId") Long seckillProductId, @Param("quantity") Integer quantity);

    /**
     * 初始化库存
     */
    @Modifying
    @Transactional
    @Query("update SeckillStock s set s.quantity = :quantity, s.version = 0 " +
           "where s.seckillProductId = :seckillProductId")
    int initStock(@Param("seckillProductId") Long seckillProductId, @Param("quantity") Integer quantity);
}