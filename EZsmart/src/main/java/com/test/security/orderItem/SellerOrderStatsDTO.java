package com.test.security.orderItem;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SellerOrderStatsDTO {
    private Long totalOrders;
    private Long totalItemsSold;
    private Double totalEarnings;
    private Long sellerId;
    private String sellerName;

    // Constructor that matches the query parameters exactly
    public SellerOrderStatsDTO(Long totalOrders, Long totalItemsSold, Double totalEarnings, 
                             Long sellerId, String sellerName) {
        this.totalOrders = totalOrders;
        this.totalItemsSold = totalItemsSold;
        this.totalEarnings = totalEarnings;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
    }
} 