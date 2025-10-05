package com.example.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品创建DTO
 */
@Data
public class ProductCreateDTO {
    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品库存
     */
    private Integer stock;

    /**
     * 商品图片
     */
    private String imageUrl;

    /**
     * 商品状态：0-下架 1-上架
     */
    private Integer status;

    /**
     * 商品权重
     */
    private Integer weight;

    /**
     * 商品详情
     */
    private ProductDetailDTO detail;

    /**
     * 商品规格列表
     */
    private List<ProductSpecDTO> specs;
}