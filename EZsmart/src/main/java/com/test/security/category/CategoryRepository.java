package com.test.security.category;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT new com.test.security.category.CategorySalesDTO(" +
           "c.id, c.name, COUNT(oi.id), SUM(oi.subtotal), c.image) " +
           "FROM OrderItem oi " +
           "JOIN oi.product p " +
           "JOIN p.category c " +
           "WHERE p.seller.id = :sellerId " +
           "AND oi.status = 'DELIVERED' " +
           "GROUP BY c.id, c.name, c.image " +
           "ORDER BY COUNT(oi.id) DESC")
    List<CategorySalesDTO> findTopSellingCategoriesBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
}