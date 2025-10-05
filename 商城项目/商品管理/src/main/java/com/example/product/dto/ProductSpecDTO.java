package com.example.product.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品规格DTO
 */
@Data
public class ProductSpecDTO {
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
     * 规格图片
     */
    private String imageUrl;

    /**
     * 规格编码
     */
    private String specCode;
}