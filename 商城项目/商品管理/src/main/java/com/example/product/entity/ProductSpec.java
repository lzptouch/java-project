package com.example.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品规格实体类
 */
@Data
@TableName("product_spec")
public class ProductSpec implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 规格ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 规格名称（如：颜色、尺码等）
     */
    private String specName;

    /**
     * 规格值（如：红色、M码等）
     */
    private String specValue;

    /**
     * 规格价格
     */
    private BigDecimal price;

    /**
     * 规格库存
     */
    private Integer stock;

    /**
     * 规格销量
     */
    private Integer sales;

    /**
     * 规格图片
     */
    private String imageUrl;

    /**
     * 规格编码
     */
    private String specCode;

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