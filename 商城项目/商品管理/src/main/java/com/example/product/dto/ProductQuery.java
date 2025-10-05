package com.example.product.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品查询DTO
 */
@Data
public class ProductQuery {
    /**
     * 商品名称（模糊查询）
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 商品状态：0-下架 1-上架
     */
    private Integer status;

    /**
     * 排序字段：price-价格 sales-销量 createTime-创建时间
     */
    private String sortBy;

    /**
     * 排序方向：asc-升序 desc-降序
     */
    private String sortOrder;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}