package com.example.seckill.controller;

import com.example.seckill.entity.SeckillProduct;
import com.example.seckill.service.SeckillProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀商品控制器
 */
@RestController
@RequestMapping("/api/seckill/products")
public class SeckillProductController {

    @Autowired
    private SeckillProductService seckillProductService;

    /**
     * 获取秒杀商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSeckillProduct(@PathVariable Long id) {
        Optional<SeckillProduct> product = seckillProductService.getSeckillProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据活动ID获取秒杀商品列表
     */
    @GetMapping("/activity/{activityId}")
    public List<SeckillProduct> getSeckillProductsByActivityId(@PathVariable Long activityId) {
        return seckillProductService.getSeckillProductsByActivityId(activityId);
    }

    /**
     * 获取活跃活动的秒杀商品
     */
    @GetMapping("/active")
    public List<SeckillProduct> getActiveSeckillProducts() {
        return seckillProductService.getActiveSeckillProducts();
    }

    /**
     * 根据商品ID和活动ID获取秒杀商品
     */
    @GetMapping("/product/{productId}/activity/{activityId}")
    public ResponseEntity<?> getSeckillProductByProductIdAndActivityId(
            @PathVariable Long productId, @PathVariable Long activityId) {
        Optional<SeckillProduct> product = seckillProductService.getSeckillProductByProductIdAndActivityId(productId, activityId);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}