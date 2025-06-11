package com.test.security.orderItem;

import com.test.security.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    
    @Query("SELECT DISTINCT oi FROM OrderItem oi " +
           "JOIN FETCH oi.order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH oi.product p " +
           "WHERE p.seller.id = :sellerId")
    List<OrderItem> findBySellerId(@Param("sellerId") int sellerId);

    @Query("SELECT new com.test.security.orderItem.SellerOrderStatsDTO(" +
           "CAST(COUNT(DISTINCT oi.order.id) AS java.lang.Long), " +  // total unique orders
           "CAST(SUM(oi.quantity) AS java.lang.Long), " +             // total items sold
           "CAST(SUM(oi.subtotal) AS java.lang.Double), " +           // total revenue
           "CAST(p.seller.id AS java.lang.Long), " +                  // seller id
           "CAST(p.seller.name AS java.lang.String)) " + // seller name
           "FROM OrderItem oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId " +
           "AND oi.status = 'DELIVERED' " +
           "GROUP BY p.seller.id, p.seller.name")
    Optional<SellerOrderStatsDTO> getSellerOrderStats(@Param("sellerId") Long sellerId);
} 