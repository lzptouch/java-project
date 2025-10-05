package com.example.seckill.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀活动实体类
 */
@Data
@Entity
@Table(name = "seckill_activity")
public class SeckillActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_name", nullable = false, length = 100)
    private String activityName;

    @Column(name = "start_time", nullable = false)
    private Date startTime;

    @Column(name = "end_time", nullable = false)
    private Date endTime;

    @Column(name = "status")
    private Integer status = 0; // 0-未开始，1-进行中，2-已结束，3-已取消

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

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
     * 检查活动是否已经开始
     */
    public boolean hasStarted() {
        return new Date().after(startTime);
    }

    /**
     * 检查活动是否已经结束
     */
    public boolean hasEnded() {
        return new Date().after(endTime);
    }

    /**
     * 检查活动是否正在进行中
     */
    public boolean isActive() {
        Date now = new Date();
        return now.after(startTime) && now.before(endTime);
    }
}