package com.example.order.feign;

import com.example.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "product-service", path = "/api/products")
public interface ProductServiceClient {

    /**
     * 根据商品ID获取商品信息
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);

    /**
     * 根据SKU ID获取商品规格信息
     * @param skuId SKU ID
     * @return 商品规格信息
     */
    @GetMapping("/sku/{skuId}")
    ProductDTO.SkuDTO getSkuById(@PathVariable("skuId") Long skuId);

    /**
     * 批量查询商品信息
     * @param productIds 商品ID列表
     * @return 商品信息列表
     */
    @GetMapping("/batch")
    Object getProductsByIds(@RequestParam("ids") String productIds);
}