package com.example.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.product.entity.Product;
import com.example.product.entity.ProductDetail;
import com.example.product.entity.ProductSpec;
import com.example.product.dto.ProductQuery;
import com.example.product.dto.ProductCreateDTO;
import com.example.product.dto.ProductDetailDTO;
import com.example.product.dto.ProductSpecDTO;
import com.example.product.mapper.ProductMapper;
import com.example.product.mapper.ProductDetailMapper;
import com.example.product.mapper.ProductSpecMapper;
import com.example.product.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductDetailMapper productDetailMapper;
    
    @Autowired
    private ProductSpecMapper productSpecMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductCreateDTO productDTO) {
        // 创建商品主表
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        productMapper.insert(product);
        
        // 创建商品详情
        if (productDTO.getDetail() != null) {
            ProductDetail detail = new ProductDetail();
            detail.setProductId(product.getId());
            BeanUtils.copyProperties(productDTO.getDetail(), detail);
            productDetailMapper.insert(detail);
        }
        
        // 创建商品规格
        if (productDTO.getSpecs() != null && !productDTO.getSpecs().isEmpty()) {
            List<ProductSpec> specs = new ArrayList<>();
            for (ProductSpecDTO specDTO : productDTO.getSpecs()) {
                ProductSpec spec = new ProductSpec();
                spec.setProductId(product.getId());
                BeanUtils.copyProperties(specDTO, spec);
                specs.add(spec);
            }
            productSpecMapper.batchInsert(specs);
        }
        
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Long id, ProductCreateDTO productDTO) {
        // 更新商品主表
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product.setId(id);
        int updateCount = productMapper.updateById(product);
        if (updateCount == 0) {
            return false;
        }
        
        // 更新商品详情
        if (productDTO.getDetail() != null) {
            ProductDetail detail = productDetailMapper.selectByProductId(id);
            if (detail != null) {
                BeanUtils.copyProperties(productDTO.getDetail(), detail);
                productDetailMapper.updateById(detail);
            } else {
                detail = new ProductDetail();
                detail.setProductId(id);
                BeanUtils.copyProperties(productDTO.getDetail(), detail);
                productDetailMapper.insert(detail);
            }
        }
        
        // 更新商品规格（先删除再插入）
        if (productDTO.getSpecs() != null) {
            productSpecMapper.deleteByProductId(id);
            List<ProductSpec> specs = new ArrayList<>();
            for (ProductSpecDTO specDTO : productDTO.getSpecs()) {
                ProductSpec spec = new ProductSpec();
                spec.setProductId(id);
                BeanUtils.copyProperties(specDTO, spec);
                specs.add(spec);
            }
            if (!specs.isEmpty()) {
                productSpecMapper.batchInsert(specs);
            }
        }
        
        return true;
    }

    @Override
    public Product getProductDetail(Long id) {
        return productMapper.selectProductDetailById(id);
    }

    @Override
    public IPage<Product> getProductList(ProductQuery query) {
        IPage<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        return productMapper.selectProductPage(page, query);
    }

    @Override
    public boolean上架Product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(1); // 1-上架
        return productMapper.updateById(product) > 0;
    }

    @Override
    public boolean下架Product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(0); // 0-下架
        return productMapper.updateById(product) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProducts(List<Long> ids) {
        // 删除商品主表（逻辑删除）
        int deleteCount = productMapper.deleteBatchIds(ids);
        
        // 删除商品详情和规格（逻辑删除）
        for (Long id : ids) {
            productDetailMapper.deleteByProductId(id);
            productSpecMapper.deleteByProductId(id);
        }
        
        return deleteCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long productId, Integer quantity) {
        // 先查询商品信息
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        
        // 扣减库存
        int result = productMapper.deductStock(productId, quantity);
        if (result > 0) {
            // 更新销量
            productMapper.updateSales(productId, quantity);
            return true;
        }
        return false;
    }

    @Override
    public boolean deductSpecStock(Long specId, Integer quantity) {
        // 此处需要实现规格库存扣减逻辑
        // 可以通过扩展ProductSpecMapper来实现
        return false;
    }

    @Override
    public boolean addStock(Long productId, Integer quantity) {
        return productMapper.addStock(productId, quantity) > 0;
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
               .eq(Product::getStatus, 1) // 只查询上架商品
               .orderByDesc(Product::getWeight, Product::getCreateTime);
        return productMapper.selectList(wrapper);
    }
}