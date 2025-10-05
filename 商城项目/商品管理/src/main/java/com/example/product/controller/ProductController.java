package com.example.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.product.entity.Product;
import com.example.product.dto.ProductQuery;
import com.example.product.dto.ProductCreateDTO;
import com.example.product.dto.Result;
import com.example.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("创建商品")
    @PostMapping
    public Result<Long> createProduct(@RequestBody ProductCreateDTO productDTO) {
        Long productId = productService.createProduct(productDTO);
        return Result.success(productId);
    }

    @ApiOperation("更新商品")
    @PutMapping("/{id}")
    public Result<Boolean> updateProduct(@PathVariable Long id, @RequestBody ProductCreateDTO productDTO) {
        boolean result = productService.updateProduct(id, productDTO);
        return Result.success(result);
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @ApiOperation("查询商品列表")
    @GetMapping
    public Result<IPage<Product>> getProductList(ProductQuery query) {
        IPage<Product> page = productService.getProductList(query);
        return Result.success(page);
    }

    @ApiOperation("上架商品")
    @PutMapping("/{id}/上架")
    public Result<Boolean>上架Product(@PathVariable Long id) {
        boolean result = productService.上架Product(id);
        return Result.success(result);
    }

    @ApiOperation("下架商品")
    @PutMapping("/{id}/下架")
    public Result<Boolean>下架Product(@PathVariable Long id) {
        boolean result = productService.下架Product(id);
        return Result.success(result);
    }

    @ApiOperation("删除商品")
    @DeleteMapping
    public Result<Boolean> deleteProducts(@RequestBody List<Long> ids) {
        boolean result = productService.deleteProducts(ids);
        return Result.success(result);
    }

    @ApiOperation("根据分类查询商品")
    @GetMapping("/category/{categoryId}")
    public Result<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryId(categoryId);
        return Result.success(products);
    }

    @ApiOperation("扣减库存")
    @PostMapping("/{id}/stock/deduct")
    public Result<Boolean> deductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean result = productService.deductStock(id, quantity);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error("库存不足");
        }
    }

    @ApiOperation("增加库存")
    @PostMapping("/{id}/stock/add")
    public Result<Boolean> addStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean result = productService.addStock(id, quantity);
        return Result.success(result);
    }
}