package com.test.security.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.id = :categoryId)")
    List<Product> findProducts(@Param("categoryId") Long categoryId);


    List<Product> findBySellerId(Long sellerId);

    void deleteByIdAndSellerId(Long productId, Long sellerId);

    Product findByIdAndSellerId(Long productId, Long sellerId);
}
