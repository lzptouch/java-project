package com.example.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.product.entity.Category;
import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 创建分类
     */
    Long createCategory(Category category);
    
    /**
     * 更新分类
     */
    boolean updateCategory(Category category);
    
    /**
     * 删除分类
     */
    boolean deleteCategory(Long id);
    
    /**
     * 获取分类详情
     */
    Category getCategoryById(Long id);
    
    /**
     * 获取所有启用的分类
     */
    List<Category> getAllEnabledCategories();
    
    /**
     * 根据父分类ID获取子分类
     */
    List<Category> getSubCategories(Long parentId);
    
    /**
     * 获取分类树结构
     */
    List<Category> getCategoryTree();
    
    /**
     * 启用分类
     */
    boolean enableCategory(Long id);
    
    /**
     * 禁用分类
     */
    boolean disableCategory(Long id);
}