package com.example.seckill.service;

import com.example.seckill.entity.SeckillUserRecord;
import com.example.seckill.repository.SeckillUserRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
import java.util.Date;

/**
 * 秒杀用户抢购记录服务类
 */
@Service
public class SeckillUserRecordService {

    @Autowired
    private SeckillUserRecordRepository recordRepository;

    /**
     * 检查用户是否已抢购过指定商品
     */
    public boolean hasUserPurchased(Long userId, Long productId, Long activityId) {
        return recordRepository.existsByUserIdAndProductIdAndActivityId(userId, productId, activityId);
    }

    /**
     * 统计用户在指定活动中的抢购次数
     */
    public int countUserPurchasesInActivity(Long userId, Long activityId) {
        return recordRepository.countByUserIdAndActivityId(userId, activityId);
    }

    /**
     * 创建用户抢购记录
     */
    @Transactional
    public SeckillUserRecord createRecord(SeckillUserRecord record) {
        return recordRepository.save(record);
    }

    /**
     * 删除指定活动的用户抢购记录
     */
    @Transactional
    public int deleteRecordsByActivityId(Long activityId) {
        return recordRepository.deleteByActivityId(activityId);
    }

    /**
     * 清理过期记录
     * @param days 保留天数
     */
    @Transactional
    public int cleanupExpiredRecords(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        Date deadline = calendar.getTime();
        return recordRepository.deleteExpiredRecords(deadline);
    }
}