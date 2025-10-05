package com.example.seckill.service;

import com.example.seckill.entity.SeckillActivity;
import com.example.seckill.repository.SeckillActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀活动服务类
 */
@Service
public class SeckillActivityService {

    @Autowired
    private SeckillActivityRepository activityRepository;

    /**
     * 根据ID获取活动
     */
    @Cacheable(value = "seckillActivity", key = "#id")
    public Optional<SeckillActivity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }

    /**
     * 获取所有活动
     */
    public List<SeckillActivity> getAllActivities() {
        return activityRepository.findAll();
    }

    /**
     * 获取进行中的活动
     */
    @Cacheable(value = "seckillActivity", key = "'active'")
    public List<SeckillActivity> getActiveActivities() {
        Date now = new Date();
        return activityRepository.findActiveActivities(now, 1);
    }

    /**
     * 获取未开始的活动
     */
    public List<SeckillActivity> getUpcomingActivities() {
        Date now = new Date();
        return activityRepository.findByStartTimeAfterAndStatus(now, 0);
    }

    /**
     * 创建活动
     */
    @Transactional
    @CacheEvict(value = {"seckillActivity"}, allEntries = true)
    public SeckillActivity createActivity(SeckillActivity activity) {
        activity.setStatus(0);
        return activityRepository.save(activity);
    }

    /**
     * 更新活动状态
     */
    @Transactional
    @CacheEvict(value = {"seckillActivity"}, allEntries = true)
    public boolean updateActivityStatus(Long id, Integer status) {
        return activityRepository.updateStatus(id, status) > 0;
    }

    /**
     * 定时任务：更新过期活动状态
     */
    @Scheduled(cron = "0/30 * * * * ?") // 每30秒执行一次
    @Transactional
    @CacheEvict(value = {"seckillActivity"}, allEntries = true)
    public void updateExpiredActivities() {
        Date now = new Date();
        int count = activityRepository.updateExpiredStatus(now);
        if (count > 0) {
            System.out.println("更新了" + count + "个过期活动的状态");
        }
    }

    /**
     * 定时任务：更新开始活动状态
     */
    @Scheduled(cron = "0/30 * * * * ?") // 每30秒执行一次
    @Transactional
    @CacheEvict(value = {"seckillActivity"}, allEntries = true)
    public void updateStartedActivities() {
        Date now = new Date();
        int count = activityRepository.updateStartedStatus(now);
        if (count > 0) {
            System.out.println("更新了" + count + "个开始活动的状态");
        }
    }
}