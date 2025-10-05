package com.example.seckill.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀订单实体类
 */
@Data
@Entity
@Table(name = "seckill_order")
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seckill_product_id", nullable = false)
    private Long seckillProductId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "seckill_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal seckillPrice;

    @Column(name = "status")
    private Integer status = 0; // 0-待支付，1-已支付，2-已取消，3-已完成

    @Column(name = "pay_deadline")
    private Date payDeadline;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seckill_product_id", insertable = false, updatable = false)
    private SeckillProduct seckillProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", insertable = false, updatable = false)
    private SeckillActivity activity;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = new Date();
        }
        if (updateTime == null) {
            updateTime = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }

    /**
     * 检查订单是否已过期
     */
    public boolean isExpired() {
        return payDeadline != null && new Date().after(payDeadline);
    }

    /**
     * 检查订单是否可支付
     */
    public boolean canPay() {
        return status == 0 && !isExpired();
    }

    /**
     * 检查订单是否已支付
     */
    public boolean isPaid() {
        return status == 1;
    }
}