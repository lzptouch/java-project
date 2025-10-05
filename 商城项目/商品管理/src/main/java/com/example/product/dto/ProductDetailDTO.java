package com.example.product.dto;

import lombok.Data;

/**
 * 商品详情DTO
 */
@Data
public class ProductDetailDTO {
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
}