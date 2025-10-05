package com.example.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product.entity.Category;
import java.util.List;

/**
 * 分类Mapper接口
 */
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 查询所有启用的分类
     */
    List<Category> selectEnabledCategories();
    
    /**
     * 根据父分类ID查询子分类
     */
    List<Category> selectByParentId(Long parentId);
    
    /**
     * 查询分类树结构
     */
    List<Category> selectCategoryTree();
}