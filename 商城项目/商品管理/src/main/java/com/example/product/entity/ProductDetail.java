package com.example.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品详情实体类
 */
@Data
@TableName("product_detail")
public class ProductDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 详情ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品详情内容（富文本）
     */
    private String detailContent;

    /**
     * 商品参数（JSON格式）
     */
    private String params;

    /**
     * 包装清单
     */
    private String packageList;

    /**
     * 售后服务说明
     */
    private String afterSaleDesc;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}