package com.example.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品DTO，用于接收商品服务返回的数据
 */
@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String image;
    private List<SkuDTO> skus;
    private String specs;
    private String detail;

    /**
     * 商品规格DTO
     */
    @Data
    public static class SkuDTO {
        private Long id;
        private Long productId;
        private String skuSpecs;
        private BigDecimal price;
        private Integer stock;
        private String image;
        private String skuCode;
    }
}