package com.example.seckill.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品实体类
 */
@Data
@Entity
@Table(name = "seckill_product")
public class SeckillProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seckill_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal seckillPrice;

    @Column(name = "seckill_stock", nullable = false)
    private Integer seckillStock;

    @Column(name = "status")
    private Integer status = 1; // 0-无效，1-有效

    @Column(name = "create_time")
    private Date createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", insertable = false, updatable = false)
    private SeckillActivity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @OneToOne(mappedBy = "seckillProduct", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private SeckillStock stock;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = new Date();
        }
    }
}