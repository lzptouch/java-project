package com.example.seckill.service;

import com.example.seckill.entity.*;
import io.lettuce.core.RedisCommandTimeoutException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 核心秒杀业务服务类
 */
@Service
public class SeckillService {

    @Autowired
    private SeckillProductService seckillProductService;

    @Autowired
    private SeckillStockService stockService;

    @Autowired
    private SeckillOrderService orderService;

    @Autowired
    private SeckillUserRecordService recordService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${seckill.redis.prefix.stock}")
    private String stockPrefix;

    @Value("${seckill.redis.prefix.userLimit}")
    private String userLimitPrefix;

    @Value("${seckill.redis.prefix.productLimit}")
    private String productLimitPrefix;

    @Value("${seckill.redis.prefix.order}")
    private String orderPrefix;

    @Value("${seckill.stock.prewarm.enabled}")
    private boolean stockPrewarmEnabled;

    @Value("${seckill.stock.prewarm.minutes}")
    private int prewarmMinutes;

    // 本地锁，防止同一进程内的重复请求
    private final Lock localLock = new ReentrantLock();

    /**
     * 秒杀商品
     */
    @Transactional
    public SeckillResult seckill(Long userId, Long seckillProductId) {
        // 1. 初步参数校验
        if (userId == null || seckillProductId == null) {
            return SeckillResult.fail("参数错误");
        }

        // 2. 尝试获取分布式锁，防止重复请求
        String lockKey = "seckill:lock:" + userId + ":" + seckillProductId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                return SeckillResult.fail("您的操作过于频繁，请稍后再试");
            }

            // 3. 检查用户是否已抢购（Redis和数据库双重校验）
            String userOrderKey = orderPrefix + userId + ":" + seckillProductId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(userOrderKey))) {
                return SeckillResult.fail("您已抢购过该商品");
            }

            // 4. 获取秒杀商品信息
            SeckillProduct seckillProduct = seckillProductService.getSeckillProductById(seckillProductId)
                    .orElseThrow(() -> new RuntimeException("秒杀商品不存在"));

            // 5. 检查活动时间
            if (!seckillProduct.getActivity().isActive()) {
                return SeckillResult.fail("活动未开始或已结束");
            }

            // 6. 检查商品状态
            if (seckillProduct.getStatus() != 1) {
                return SeckillResult.fail("商品已下架");
            }

            // 7. 检查用户是否已抢购（数据库校验）
            if (recordService.hasUserPurchased(userId, seckillProduct.getProduct().getId(), seckillProduct.getActivity().getId())) {
                return SeckillResult.fail("您已抢购过该商品");
            }

            // 8. 检查用户购买次数限制（Redis实现）
            String userLimitKey = userLimitPrefix + userId + ":" + seckillProduct.getActivity().getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(userLimitKey))) {
                return SeckillResult.fail("您已达到活动购买限制");
            }

            // 9. Redis预扣减库存
            String stockKey = stockPrefix + seckillProductId;
            try {
                Long stock = redisTemplate.opsForValue().decrement(stockKey);
                if (stock == null || stock < 0) {
                    // 库存不足，回滚
                    redisTemplate.opsForValue().increment(stockKey);
                    return SeckillResult.fail("商品已售罄");
                }
            } catch (RedisCommandTimeoutException e) {
                // Redis超时，降级到数据库
                return handleDatabaseSeckill(userId, seckillProduct);
            }

            // 10. 数据库层面扣减库存（乐观锁）
            SeckillStock stock = stockService.getStockBySeckillProductId(seckillProductId)
                    .orElseThrow(() -> new RuntimeException("库存信息不存在"));

            boolean stockDeducted = stockService.deductStockWithOptimisticLock(seckillProductId, stock.getVersion());
            if (!stockDeducted) {
                // 库存扣减失败，回滚Redis库存
                redisTemplate.opsForValue().increment(stockKey);
                return SeckillResult.fail("商品已售罄");
            }

            // 11. 创建订单
            SeckillOrder order = new SeckillOrder();
            order.setUserId(userId);
            order.setProductId(seckillProduct.getProduct().getId());
            order.setSeckillProductId(seckillProductId);
            order.setActivityId(seckillProduct.getActivity().getId());
            order.setSeckillPrice(seckillProduct.getSeckillPrice());
            order.setCreateTime(new Date());
            SeckillOrder createdOrder = orderService.createOrder(order);

            // 12. 记录用户抢购
            SeckillUserRecord record = new SeckillUserRecord();
            record.setUserId(userId);
            record.setProductId(seckillProduct.getProduct().getId());
            record.setActivityId(seckillProduct.getActivity().getId());
            record.setCreateTime(new Date());
            recordService.createRecord(record);

            // 13. Redis记录用户已抢购
            redisTemplate.opsForValue().set(userOrderKey, createdOrder.getId(), 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(userLimitKey, 1, 24, TimeUnit.HOURS);

            return SeckillResult.success(createdOrder);
        } catch (Exception e) {
            return SeckillResult.fail("秒杀失败：" + e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 降级到数据库层面的秒杀处理
     */
    @Transactional
    public SeckillResult handleDatabaseSeckill(Long userId, SeckillProduct seckillProduct) {
        // 检查库存
        if (!stockService.checkStock(seckillProduct.getId(), 1)) {
            return SeckillResult.fail("商品已售罄");
        }

        // 使用悲观锁处理库存扣减
        try {
            localLock.lock();
            SeckillStock stock = stockService.getStockWithLock(seckillProduct.getId());
            if (stock.getQuantity() <= 0) {
                return SeckillResult.fail("商品已售罄");
            }

            // 扣减库存
            boolean stockDeducted = stockService.deductStock(seckillProduct.getId(), 1);
            if (!stockDeducted) {
                return SeckillResult.fail("商品已售罄");
            }

            // 创建订单
            SeckillOrder order = new SeckillOrder();
            order.setUserId(userId);
            order.setProductId(seckillProduct.getProduct().getId());
            order.setSeckillProductId(seckillProduct.getId());
            order.setActivityId(seckillProduct.getActivity().getId());
            order.setSeckillPrice(seckillProduct.getSeckillPrice());
            order.setCreateTime(new Date());
            SeckillOrder createdOrder = orderService.createOrder(order);

            // 记录用户抢购
            SeckillUserRecord record = new SeckillUserRecord();
            record.setUserId(userId);
            record.setProductId(seckillProduct.getProduct().getId());
            record.setActivityId(seckillProduct.getActivity().getId());
            record.setCreateTime(new Date());
            recordService.createRecord(record);

            return SeckillResult.success(createdOrder);
        } finally {
            localLock.unlock();
        }
    }

    /**
     * 库存预热
     */
    @Scheduled(cron = "0/60 * * * * ?") // 每分钟执行一次
    public void prewarmStock() {
        if (!stockPrewarmEnabled) {
            return;
        }

        List<SeckillProduct> products = seckillProductService.getProductsToPrewarm(prewarmMinutes);
        for (SeckillProduct product : products) {
            String stockKey = stockPrefix + product.getId();
            // 将库存加载到Redis
            redisTemplate.opsForValue().set(stockKey, product.getStock().getQuantity(), 2, TimeUnit.HOURS);
            System.out.println("库存预热：商品ID=" + product.getId() + "，库存=" + product.getStock().getQuantity());
        }
    }

    /**
     * 秒杀结果类
     */
    public static class SeckillResult {
        private boolean success;
        private String message;
        private SeckillOrder order;

        private SeckillResult(boolean success, String message, SeckillOrder order) {
            this.success = success;
            this.message = message;
            this.order = order;
        }

        public static SeckillResult success(SeckillOrder order) {
            return new SeckillResult(true, "秒杀成功", order);
        }

        public static SeckillResult fail(String message) {
            return new SeckillResult(false, message, null);
        }

        // getter方法
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public SeckillOrder getOrder() {
            return order;
        }
    }
}