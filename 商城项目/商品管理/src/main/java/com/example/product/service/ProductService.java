package com.example.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.product.entity.Product;
import com.example.product.dto.ProductQuery;
import com.example.product.dto.ProductCreateDTO;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {
    
    /**
     * 创建商品
     */
    Long createProduct(ProductCreateDTO productDTO);
    
    /**
     * 更新商品
     */
    boolean updateProduct(Long id, ProductCreateDTO productDTO);
    
    /**
     * 获取商品详情
     */
    Product getProductDetail(Long id);
    
    /**
     * 查询商品列表
     */
    IPage<Product> getProductList(ProductQuery query);
    
    /**
     * 上架商品
     */
    boolean上架Product(Long id);
    
    /**
     * 下架商品
     */
    boolean下架Product(Long id);
    
    /**
     * 批量删除商品
     */
    boolean deleteProducts(List<Long> ids);
    
    /**
     * 扣减库存
     */
    boolean deductStock(Long productId, Integer quantity);
    
    /**
     * 扣减规格库存
     */
    boolean deductSpecStock(Long specId, Integer quantity);
    
    /**
     * 增加库存
     */
    boolean addStock(Long productId, Integer quantity);
    
    /**
     * 根据分类查询商品
     */
    List<Product> getProductsByCategoryId(Long categoryId);
}