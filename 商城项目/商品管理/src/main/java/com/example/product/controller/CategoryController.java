package com.example.product.controller;

import com.example.product.entity.Category;
import com.example.product.dto.Result;
import com.example.product.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@Api(tags = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("创建分类")
    @PostMapping
    public Result<Long> createCategory(@RequestBody Category category) {
        Long categoryId = categoryService.createCategory(category);
        return Result.success(categoryId);
    }

    @ApiOperation("更新分类")
    @PutMapping("/{id}")
    public Result<Boolean> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        boolean result = categoryService.updateCategory(category);
        return Result.success(result);
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        boolean result = categoryService.deleteCategory(id);
        if (!result) {
            return Result.error("该分类下有子分类，无法删除");
        }
        return Result.success(true);
    }

    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @ApiOperation("获取所有启用的分类")
    @GetMapping("/enabled")
    public Result<List<Category>> getAllEnabledCategories() {
        List<Category> categories = categoryService.getAllEnabledCategories();
        return Result.success(categories);
    }

    @ApiOperation("获取子分类")
    @GetMapping("/{parentId}/children")
    public Result<List<Category>> getSubCategories(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getSubCategories(parentId);
        return Result.success(categories);
    }

    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public Result<List<Category>> getCategoryTree() {
        List<Category> categories = categoryService.getCategoryTree();
        return Result.success(categories);
    }

    @ApiOperation("启用分类")
    @PutMapping("/{id}/enable")
    public Result<Boolean> enableCategory(@PathVariable Long id) {
        boolean result = categoryService.enableCategory(id);
        return Result.success(result);
    }

    @ApiOperation("禁用分类")
    @PutMapping("/{id}/disable")
    public Result<Boolean> disableCategory(@PathVariable Long id) {
        boolean result = categoryService.disableCategory(id);
        return Result.success(result);
    }
}