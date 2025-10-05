package com.example.seckill.service;

import com.example.seckill.entity.SeckillStock;
import com.example.seckill.repository.SeckillStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * 秒杀库存服务类
 */
@Service
public class SeckillStockService {

    @Autowired
    private SeckillStockRepository stockRepository;

    /**
     * 根据秒杀商品ID获取库存
     */
    public Optional<SeckillStock> getStockBySeckillProductId(Long seckillProductId) {
        return Optional.ofNullable(stockRepository.findBySeckillProductId(seckillProductId));
    }

    /**
     * 使用悲观锁获取库存
     */
    @Transactional
    public SeckillStock getStockWithLock(Long seckillProductId) {
        return stockRepository.findBySeckillProductId(seckillProductId);
    }

    /**
     * 使用乐观锁扣减库存
     */
    @Transactional
    public boolean deductStockWithOptimisticLock(Long seckillProductId, Integer version) {
        int result = stockRepository.deductStockWithOptimisticLock(seckillProductId, version);
        return result > 0;
    }

    /**
     * 直接扣减库存（用于测试或特殊场景）
     */
    @Transactional
    public boolean deductStock(Long seckillProductId, Integer quantity) {
        int result = stockRepository.deductStock(seckillProductId, quantity);
        return result > 0;
    }

    /**
     * 增加库存
     */
    @Transactional
    public boolean addStock(Long seckillProductId, Integer quantity) {
        int result = stockRepository.addStock(seckillProductId, quantity);
        return result > 0;
    }

    /**
     * 初始化库存
     */
    @Transactional
    public boolean initStock(Long seckillProductId, Integer quantity) {
        int result = stockRepository.initStock(seckillProductId, quantity);
        return result > 0;
    }

    /**
     * 检查库存是否充足
     */
    public boolean checkStock(Long seckillProductId, Integer quantity) {
        SeckillStock stock = stockRepository.findBySeckillProductId(seckillProductId);
        return stock != null && stock.getQuantity() >= quantity;
    }
}