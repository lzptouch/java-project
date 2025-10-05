package com.example.seckill.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀库存实体类（独立表，用于并发扣减）
 */
@Data
@Entity
@Table(name = "seckill_stock")
public class SeckillStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seckill_product_id", unique = true, nullable = false)
    private Long seckillProductId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Version
    @Column(name = "version")
    private Integer version = 0;

    @Column(name = "update_time")
    private Date updateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seckill_product_id", insertable = false, updatable = false)
    private SeckillProduct seckillProduct;

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }

    /**
     * 检查库存是否充足
     */
    public boolean isStockSufficient() {
        return quantity > 0;
    }

    /**
     * 扣减库存
     */
    public boolean deductStock() {
        if (isStockSufficient()) {
            quantity--;
            return true;
        }
        return false;
    }
}