package com.example.seckill.service;

import com.example.seckill.entity.Product;
import com.example.seckill.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 商品服务类
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 根据ID获取商品
     */
    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 获取所有上架商品
     */
    @Cacheable(value = "products", key = "'active'")
    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatus(1);
    }

    /**
     * 搜索商品
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByProductNameContainingAndStatus(keyword, 1);
    }

    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(Product product) {
        product.setStatus(1);
        return productRepository.save(product);
    }

    /**
     * 更新商品库存
     */
    @Transactional
    public boolean updateProductStock(Long id, Integer quantity) {
        return productRepository.updateStock(id, quantity) > 0;
    }

    /**
     * 批量更新商品状态
     */
    @Transactional
    public int batchUpdateProductStatus(List<Long> ids, Integer status) {
        return productRepository.batchUpdateStatus(ids, status);
    }
}