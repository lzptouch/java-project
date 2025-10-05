package com.example.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.product.entity.Category;
import com.example.product.mapper.CategoryMapper;
import com.example.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Long createCategory(Category category) {
        // 设置默认值
        if (category.getParentId() == null) {
            category.setParentId(0L); // 顶级分类
            category.setLevel(1);
        } else {
            // 如果是子分类，查询父分类级别
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent != null) {
                category.setLevel(parent.getLevel() + 1);
            } else {
                category.setLevel(1);
                category.setParentId(0L);
            }
        }
        
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public boolean updateCategory(Category category) {
        // 不允许修改父分类，避免破坏分类结构
        Category updateCategory = new Category();
        updateCategory.setId(category.getId());
        updateCategory.setName(category.getName());
        updateCategory.setIcon(category.getIcon());
        updateCategory.setWeight(category.getWeight());
        updateCategory.setStatus(category.getStatus());
        
        return categoryMapper.updateById(updateCategory) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        int count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            return false; // 有子分类，不允许删除
        }
        
        // 逻辑删除分类
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> getAllEnabledCategories() {
        return categoryMapper.selectEnabledCategories();
    }

    @Override
    public List<Category> getSubCategories(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public List<Category> getCategoryTree() {
        return categoryMapper.selectCategoryTree();
    }

    @Override
    public boolean enableCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(1); // 1-启用
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean disableCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(0); // 0-禁用
        return categoryMapper.updateById(category) > 0;
    }
}