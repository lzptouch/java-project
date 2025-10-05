package com.example.seckill.repository;

import com.example.seckill.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;

/**
 * 商品数据访问接口
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 查找上架的商品列表
     */
    List<Product> findByStatus(Integer status);

    /**
     * 根据商品名称模糊查询
     */
    List<Product> findByProductNameContainingAndStatus(String keyword, Integer status);

    /**
     * 更新商品库存
     */
    @Modifying
    @Transactional
    @Query("update Product p set p.stock = p.stock - :quantity where p.id = :id and p.stock >= :quantity")
    int updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 批量更新商品状态
     */
    @Modifying
    @Transactional
    @Query("update Product p set p.status = :status where p.id in :ids")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
}