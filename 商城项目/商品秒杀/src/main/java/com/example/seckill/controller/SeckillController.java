package com.example.seckill.controller;

import com.example.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 核心秒杀接口控制器
 */
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 秒杀接口
     * @param userId 用户ID
     * @param seckillProductId 秒杀商品ID
     * @return 秒杀结果
     */
    @PostMapping("/do/{userId}/{seckillProductId}")
    public ResponseEntity<?> doSeckill(@PathVariable Long userId, @PathVariable Long seckillProductId) {
        SeckillService.SeckillResult result = seckillService.seckill(userId, seckillProductId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}