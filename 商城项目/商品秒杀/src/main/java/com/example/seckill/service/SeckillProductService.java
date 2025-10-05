package com.example.seckill.service;

import com.example.seckill.entity.SeckillProduct;
import com.example.seckill.repository.SeckillProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀商品服务类
 */
@Service
public class SeckillProductService {

    @Autowired
    private SeckillProductRepository productRepository;

    /**
     * 根据ID获取秒杀商品
     */
    @Cacheable(value = "seckillProduct", key = "#id")
    public Optional<SeckillProduct> getSeckillProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 根据活动ID获取秒杀商品列表
     */
    public List<SeckillProduct> getSeckillProductsByActivityId(Long activityId) {
        return productRepository.findByActivityIdAndStatus(activityId, 1);
    }

    /**
     * 根据商品ID和活动ID获取秒杀商品
     */
    public Optional<SeckillProduct> getSeckillProductByProductIdAndActivityId(Long productId, Long activityId) {
        return productRepository.findByProductIdAndActivityId(productId, activityId);
    }

    /**
     * 获取活跃活动的秒杀商品
     */
    @Cacheable(value = "seckillProduct", key = "'active'")
    public List<SeckillProduct> getActiveSeckillProducts() {
        return productRepository.findActiveSeckillProducts();
    }

    /**
     * 获取需要预热的秒杀商品
     * @param prewarmMinutes 提前预热的分钟数
     */
    public List<SeckillProduct> getProductsToPrewarm(int prewarmMinutes) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, prewarmMinutes);
        Date prewarmTime = calendar.getTime();
        return productRepository.findProductsToPrewarm(now, prewarmTime);
    }
}