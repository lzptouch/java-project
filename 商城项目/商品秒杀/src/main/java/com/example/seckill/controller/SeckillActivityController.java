package com.example.seckill.controller;

import com.example.seckill.entity.SeckillActivity;
import com.example.seckill.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀活动控制器
 */
@RestController
@RequestMapping("/api/seckill/activities")
public class SeckillActivityController {

    @Autowired
    private SeckillActivityService activityService;

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivity(@PathVariable Long id) {
        Optional<SeckillActivity> activity = activityService.getActivityById(id);
        return activity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有活动
     */
    @GetMapping
    public List<SeckillActivity> getAllActivities() {
        return activityService.getAllActivities();
    }

    /**
     * 获取进行中的活动
     */
    @GetMapping("/active")
    public List<SeckillActivity> getActiveActivities() {
        return activityService.getActiveActivities();
    }

    /**
     * 获取未开始的活动
     */
    @GetMapping("/upcoming")
    public List<SeckillActivity> getUpcomingActivities() {
        return activityService.getUpcomingActivities();
    }

    /**
     * 创建活动
     */
    @PostMapping
    public SeckillActivity createActivity(@RequestBody SeckillActivity activity) {
        return activityService.createActivity(activity);
    }

    /**
     * 更新活动状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean updated = activityService.updateActivityStatus(id, status);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}