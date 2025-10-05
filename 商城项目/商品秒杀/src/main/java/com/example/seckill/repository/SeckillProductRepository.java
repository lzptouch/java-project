package com.example.seckill.repository;

import com.example.seckill.entity.SeckillProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀商品数据访问接口
 */
@Repository
public interface SeckillProductRepository extends JpaRepository<SeckillProduct, Long> {

    /**
     * 根据活动ID查找秒杀商品
     */
    List<SeckillProduct> findByActivityIdAndStatus(Long activityId, Integer status);

    /**
     * 根据商品ID和活动ID查找秒杀商品
     */
    Optional<SeckillProduct> findByProductIdAndActivityId(Long productId, Long activityId);

    /**
     * 根据活动ID和商品ID列表查找秒杀商品
     */
    List<SeckillProduct> findByActivityIdAndProductIdIn(Long activityId, List<Long> productIds);

    /**
     * 查找需要预热的秒杀商品（活动开始前指定时间内）
     */
    @Query("select sp from SeckillProduct sp " +
           "join sp.activity a " +
           "where a.status = 0 and sp.status = 1 " +
           "and a.startTime <= :prewarmTime and a.startTime > :now")
    List<SeckillProduct> findProductsToPrewarm(@Param("now") java.util.Date now, @Param("prewarmTime") java.util.Date prewarmTime);

    /**
     * 获取活跃活动的秒杀商品（包含活动信息和库存信息）
     */
    @Query("select sp from SeckillProduct sp " +
           "join fetch sp.activity a " +
           "join fetch sp.stock s " +
           "where a.status = 1 and sp.status = 1")
    List<SeckillProduct> findActiveSeckillProducts();
}