package com.example.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.product.entity.Product;
import com.example.product.dto.ProductQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 商品Mapper接口
 */
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 分页查询商品列表
     */
    IPage<Product> selectProductPage(IPage<Product> page, @Param("query") ProductQuery query);
    
    /**
     * 根据ID查询商品详情（包含分类信息）
     */
    Product selectProductDetailById(@Param("id") Long id);
    
    /**
     * 扣减商品库存
     */
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 增加商品库存
     */
    int addStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 更新商品销量
     */
    int updateSales(@Param("id") Long id, @Param("quantity") Integer quantity);
}