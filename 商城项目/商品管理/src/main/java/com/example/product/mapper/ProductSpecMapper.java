package com.example.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product.entity.ProductSpec;
import java.util.List;

/**
 * 商品规格Mapper接口
 */
public interface ProductSpecMapper extends BaseMapper<ProductSpec> {
    
    /**
     * 根据商品ID查询规格列表
     */
    List<ProductSpec> selectByProductId(Long productId);
    
    /**
     * 批量插入规格
     */
    int batchInsert(List<ProductSpec> specs);
    
    /**
     * 根据商品ID删除所有规格
     */
    int deleteByProductId(Long productId);
}