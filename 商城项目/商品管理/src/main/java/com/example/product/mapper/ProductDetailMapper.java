package com.example.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product.entity.ProductDetail;

/**
 * 商品详情Mapper接口
 */
public interface ProductDetailMapper extends BaseMapper<ProductDetail> {
    
    /**
     * 根据商品ID查询详情
     */
    ProductDetail selectByProductId(Long productId);
}