package com.example.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.order.entity.OrderMain;
import com.example.order.entity.OrderItem;
import com.example.order.entity.OrderPayment;
import com.example.order.mapper.OrderMainMapper;
import com.example.order.mapper.OrderItemMapper;
import com.example.order.mapper.OrderPaymentMapper;
import com.example.order.service.OrderService;
import com.example.order.dto.OrderCreateDTO;
import com.example.order.dto.OrderQueryDTO;
import com.example.order.dto.OrderDetailDTO;
import com.example.order.dto.Result;
import com.example.order.dto.ProductDTO;
import com.example.order.feign.ProductServiceClient;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMainMapper, OrderMain> implements OrderService {

    @Autowired
    private OrderMainMapper orderMainMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductServiceClient productServiceClient;

    private Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    // 订单状态常量
    private static final Integer ORDER_STATUS_DRAFT = 0;
    private static final Integer ORDER_STATUS_PENDING_PAY = 1;
    private static final Integer ORDER_STATUS_PAID = 2;
    private static final Integer ORDER_STATUS_SHIPPED = 3;
    private static final Integer ORDER_STATUS_COMPLETED = 4;
    private static final Integer ORDER_STATUS_CANCELLED = 5;

    // 支付状态常量
    private static final Integer PAY_STATUS_PENDING = 0;
    private static final Integer PAY_STATUS_SUCCESS = 1;
    private static final Integer PAY_STATUS_FAILED = 2;

    // Redis key 前缀
    private static final String ORDER_REQUEST_ID_PREFIX = "order:request:";
    private static final String ORDER_CACHE_PREFIX = "order:detail:";

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO orderCreateDTO) {
        // 1. 幂等性校验
        String requestId = orderCreateDTO.getRequestId();
        if (requestId != null) {
            Boolean exists = redisTemplate.hasKey(ORDER_REQUEST_ID_PREFIX + requestId);
            if (exists != null && exists) {
                log.warn("订单创建请求重复，requestId: {}", requestId);
                throw new RuntimeException("请勿重复提交订单");
            }
            // 设置请求ID，过期时间30分钟
            redisTemplate.opsForValue().set(ORDER_REQUEST_ID_PREFIX + requestId, requestId, 30, TimeUnit.MINUTES);
        }

        // 2. 生成订单号
        Long orderId = snowflake.nextId();

        // 3. 创建订单主表
        OrderMain orderMain = new OrderMain();
        orderMain.setOrderId(orderId);
        orderMain.setUserId(orderCreateDTO.getUserId());
        orderMain.setStatus(ORDER_STATUS_PENDING_PAY);
        orderMain.setCreateTime(new Date());
        orderMain.setUpdateTime(new Date());

        // 4. 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : orderCreateDTO.getItems()) {
            // 调用商品服务获取SKU信息
            ProductDTO.SkuDTO skuDTO = productServiceClient.getSkuById(itemDTO.getSkuId());
            if (skuDTO == null) {
                throw new RuntimeException("商品规格不存在: " + itemDTO.getSkuId());
            }
            
            // 检查库存
            if (skuDTO.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + skuDTO.getSkuSpecs());
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setSkuId(itemDTO.getSkuId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(skuDTO.getPrice());
            orderItem.setTotalPrice(skuDTO.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
            
            // 设置商品名称和规格信息
            orderItem.setProductName(skuDTO.getSkuSpecs());
            orderItem.setSkuSpecs(skuDTO.getSkuSpecs());
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        orderMain.setTotalAmount(totalAmount);
        orderMain.setPayAmount(totalAmount); // 暂时不考虑优惠

        // 5. 插入订单主表
        orderMainMapper.insert(orderMain);

        // 6. 批量插入订单明细
        orderItemMapper.batchInsert(orderItems);

        // 7. 创建支付记录
        OrderPayment payment = new OrderPayment();
        payment.setOrderId(orderId);
        payment.setPayAmount(totalAmount);
        payment.setPayType(orderCreateDTO.getPayType());
        payment.setStatus(PAY_STATUS_PENDING);
        payment.setCreateTime(new Date());
        payment.setUpdateTime(new Date());
        orderPaymentMapper.insert(payment);

        // 8. TODO: 调用库存服务锁定库存

        log.info("订单创建成功，orderId: {}", orderId);
        return orderId;
    }

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId) {
        // 1. 先从缓存获取
        OrderDetailDTO detailDTO = (OrderDetailDTO) redisTemplate.opsForValue().get(ORDER_CACHE_PREFIX + orderId);
        if (detailDTO != null) {
            return detailDTO;
        }

        // 2. 从数据库查询
        OrderMain orderMain = orderMainMapper.selectOrderDetailByOrderId(orderId);
        if (orderMain == null) {
            throw new RuntimeException("订单不存在");
        }

        // 3. 构建响应DTO
        detailDTO = new OrderDetailDTO();
        detailDTO.setOrderId(orderMain.getOrderId());
        detailDTO.setUserId(orderMain.getUserId());
        detailDTO.setTotalAmount(orderMain.getTotalAmount());
        detailDTO.setPayAmount(orderMain.getPayAmount());
        detailDTO.setStatus(orderMain.getStatus());
        detailDTO.setStatusText(getStatusText(orderMain.getStatus()));
        detailDTO.setPayTime(orderMain.getPayTime());
        detailDTO.setShipTime(orderMain.getShipTime());
        detailDTO.setReceiveTime(orderMain.getReceiveTime());
        detailDTO.setCreateTime(orderMain.getCreateTime());
        detailDTO.setOrderItems(orderMain.getOrderItems());
        detailDTO.setPayment(orderMain.getPayment());
        detailDTO.setAfterSale(orderMain.getAfterSale());

        // 4. 设置缓存，10分钟过期
        redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + orderId, detailDTO, 10, TimeUnit.MINUTES);

        return detailDTO;
    }

    @Override
    public Map<String, Object> getOrderList(OrderQueryDTO queryDTO) {
        List<OrderMain> orderList = orderMainMapper.selectOrderList(queryDTO);
        // TODO: 获取总数，这里简化处理
        Map<String, Object> result = new HashMap<>();
        result.put("list", orderList);
        result.put("total", orderList.size());
        return result;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, Long userId) {
        // 1. 查询订单
        OrderMain orderMain = orderMainMapper.selectById(orderId);
        if (orderMain == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 验证用户权限
        if (!Objects.equals(orderMain.getUserId(), userId)) {
            throw new RuntimeException("无权操作该订单");
        }

        // 3. 验证订单状态
        if (!Objects.equals(orderMain.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new RuntimeException("订单状态不允许取消");
        }

        // 4. 更新订单状态
        int rows = orderMainMapper.updateOrderStatus(orderId, ORDER_STATUS_CANCELLED, ORDER_STATUS_PENDING_PAY);
        if (rows == 0) {
            throw new RuntimeException("订单状态更新失败");
        }

        // 5. TODO: 调用库存服务解锁库存

        // 6. 清除缓存
        redisTemplate.delete(ORDER_CACHE_PREFIX + orderId);

        log.info("订单取消成功，orderId: {}", orderId);
        return true;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public boolean paySuccess(Long orderId, String payNo, String paymentMethod) {
        // 1. 查询订单
        OrderMain orderMain = orderMainMapper.selectById(orderId);
        if (orderMain == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 验证订单状态
        if (!Objects.equals(orderMain.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new RuntimeException("订单状态不允许支付");
        }

        // 3. 更新订单状态
        int rows = orderMainMapper.updateOrderStatus(orderId, ORDER_STATUS_PAID, ORDER_STATUS_PENDING_PAY);
        if (rows == 0) {
            throw new RuntimeException("订单状态更新失败");
        }

        // 4. 更新支付记录
        OrderPayment payment = orderPaymentMapper.selectByOrderId(orderId);
        if (payment != null) {
            payment.setPayNo(payNo);
            payment.setStatus(PAY_STATUS_SUCCESS);
            payment.setCallbackTime(DateUtil.now());
            payment.setUpdateTime(new Date());
            orderPaymentMapper.updateById(payment);
        }

        // 5. 调用商品服务扣减库存
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : orderItems) {
            // TODO: 调用商品服务扣减库存
            // 这里应该调用商品服务的扣减库存接口
            log.info("扣减库存: skuId={}, quantity={}", item.getSkuId(), item.getQuantity());
        }

        // 6. 清除缓存
        redisTemplate.delete(ORDER_CACHE_PREFIX + orderId);

        log.info("订单支付成功，orderId: {}, payNo: {}", orderId, payNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long orderId, String logisticsNo) {
        // 1. 查询订单
        OrderMain orderMain = orderMainMapper.selectById(orderId);
        if (orderMain == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 验证订单状态
        if (!Objects.equals(orderMain.getStatus(), ORDER_STATUS_PAID)) {
            throw new RuntimeException("订单状态不允许发货");
        }

        // 3. 更新订单状态
        orderMain.setStatus(ORDER_STATUS_SHIPPED);
        orderMain.setShipTime(new Date());
        orderMain.setUpdateTime(new Date());
        orderMainMapper.updateById(orderMain);

        // 4. 清除缓存
        redisTemplate.delete(ORDER_CACHE_PREFIX + orderId);

        log.info("订单发货成功，orderId: {}, logisticsNo: {}", orderId, logisticsNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(Long orderId, Long userId) {
        // 1. 查询订单
        OrderMain orderMain = orderMainMapper.selectById(orderId);
        if (orderMain == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 验证用户权限
        if (!Objects.equals(orderMain.getUserId(), userId)) {
            throw new RuntimeException("无权操作该订单");
        }

        // 3. 验证订单状态
        if (!Objects.equals(orderMain.getStatus(), ORDER_STATUS_SHIPPED)) {
            throw new RuntimeException("订单状态不允许确认收货");
        }

        // 4. 更新订单状态
        orderMain.setStatus(ORDER_STATUS_COMPLETED);
        orderMain.setReceiveTime(new Date());
        orderMain.setUpdateTime(new Date());
        orderMainMapper.updateById(orderMain);

        // 5. 清除缓存
        redisTemplate.delete(ORDER_CACHE_PREFIX + orderId);

        log.info("订单确认收货成功，orderId: {}", orderId);
        return true;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void processTimeoutOrders() {
        log.info("开始处理超时订单");
        try {
            // 查询30分钟前创建的待支付订单
            List<OrderMain> timeoutOrders = orderMainMapper.selectTimeoutOrders(30);
            for (OrderMain order : timeoutOrders) {
                try {
                    // 尝试取消订单
                    cancelOrder(order.getOrderId(), order.getUserId());
                    log.info("超时订单自动取消成功，orderId: {}", order.getOrderId());
                } catch (Exception e) {
                    log.error("超时订单自动取消失败，orderId: {}", order.getOrderId(), e);
                }
            }
        } catch (Exception e) {
            log.error("处理超时订单异常", e);
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "草稿"; 
            case 1: return "待支付"; 
            case 2: return "已支付"; 
            case 3: return "已发货"; 
            case 4: return "已完成"; 
            case 5: return "已取消"; 
            default: return "未知状态"; 
        }
    }
}